import requests
import os
import json
import logging
import re
from typing import List, Dict, Optional, Set


PROJECT_KEY = "NMS"
JIRA_USER = os.getenv("JIRA_USER")
JIRA_API_TOKEN = os.getenv("JIRA_API_TOKEN")
JIRA_URL = os.getenv("JIRA_URL")
SECURITY_LEVEL = "TOG (migrated)"


PRIORITY_MAP = {
    "CRITICAL": "Critical",
    "HIGH": "High",
    "MEDIUM": "Medium",
    "LOW": "Low",
    "Trivial": "Trivial"
}

# Blocklist of package names or vulnerability IDs to ignore
BLOCKLIST = {
    "libxml2", 
    "openssl",
    "golang.org/x/net",
    "org.liquibase:liquibase-core",
    "org.apache.cxf:cxf-core",
    "net.minidev:json-smart",
    "io.netty:netty-handler",
    "python-unversioned-command",
    "openssl-libs",
    "org.apache.camel:camel-core",
    "CVE-2022-41721",
    "CVE-2022-41723",
    "CVE-2022-0839",
    "CVE-2025-23184",
    "CVE-2019-0188",
    "CVE-2024-57699",
    "CVE-2025-24970",
    "CVE-2023-6597",
    "CVE-2024-12797",
    "CVE-2024-56171",
    "CVE-2023-39325",
    "CVE-2024-45338",
    "CVE-2024-2961", 
    "CVE-2020-11971",
}

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    handlers=[
        logging.FileHandler('jira_integration.log'),
        logging.StreamHandler()
    ]
)

class JiraIntegrator:
    def __init__(self):
        self.processed_packages: Set[str] = set()
        self.processed_issues: Set[str] = set()
        self.session = requests.Session()
        self.session.auth = (JIRA_USER, JIRA_API_TOKEN)
        self.session.headers.update({'Content-Type': 'application/json'})
        self.blocklist = BLOCKLIST

    def is_blocked(self, package_name: str, vulnerabilities: List[Dict]) -> bool:

        if package_name in self.blocklist:
            return True
        return any(v['VulnerabilityID'] in self.blocklist for v in vulnerabilities)

    def normalize_package_name(self, name: str) -> str:

        name = re.sub(r'[:@/]', '-', name)  # Replace special chars
        name = re.sub(r'[-_]\d+.*$', '', name)  # Remove version numbers
        return name.lower().strip()

    def escape_jql_value(self, text: str) -> str:
        return text.translate(str.maketrans({
            '"': r'\"',
            "'": r"\'",
            "(": r"\(",
            ")": r"\)",
            "[": r"\[",
            "]": r"\]",
            "~": r"\~"
        }))

    def build_safe_jql(self, package_name: str, cve_ids: List[str]) -> str:
        safe_pkg = self.escape_jql_value(package_name)
        cve_conditions = []
        
        for cve in cve_ids:
            safe_cve = self.escape_jql_value(cve)
            cve_conditions.append(f'description ~ "{safe_cve}"')
            cve_conditions.append(f'description ~ "\\\\"{safe_cve}\\\\""')
            cve_conditions.append(f'description ~ "{safe_cve}[^a-zA-Z0-9]"')
            cve_conditions.append(f'description ~ "[^a-zA-Z0-9]{safe_cve}"')
        
        return (
            f'project = {PROJECT_KEY} AND '
            f'(summary ~ "{safe_pkg}" OR description ~ "{safe_pkg}") AND '
            f'({" OR ".join(cve_conditions)}) AND '
            f'resolution IS EMPTY'
        )

    def find_existing_issue(self, package_name: str, cve_ids: List[str]) -> Optional[Dict]:

        normalized_pkg = self.normalize_package_name(package_name)
        jql = self.build_safe_jql(normalized_pkg, cve_ids)
        
        try:
            response = self.session.get(
                f"{JIRA_URL}/rest/api/2/search",
                params={'jql': jql, 'maxResults': 1},
                timeout=30
            )
            response.raise_for_status()
            issues = response.json().get('issues', [])
            return issues[0] if issues else None
        except requests.exceptions.HTTPError as e:
            logging.error(f"Jira search failed (HTTP {e.response.status_code}): {e.response.text}")
            logging.debug(f"Problematic JQL: {jql}")
        except Exception as e:
            logging.error(f"Error searching Jira: {str(e)}")
        return None

    def create_issue(self, package_name: str, vulnerabilities: List[Dict]) -> Optional[str]:

        severities = {v['Severity'] for v in vulnerabilities}
        priority = "Medium"
        if "CRITICAL" in severities:
            priority = "Critical"
        elif "HIGH" in severities:
            priority = "High"
        elif "LOW" in severities:
            priority = "Low"
        elif "Trivial" in severities:
            priority = "Trivial"

        vuln_details = "\n".join(self.format_vulnerability(v) for v in vulnerabilities)
        description = (
            f"*Package Name:* {package_name}\n\n"
            f"*Vulnerabilities:*\n{vuln_details}"
        )

        payload = {
            "fields": {
                "project": {"key": PROJECT_KEY},
                "summary": f"Vulnerability: {package_name}",
                "description": description,
                "issuetype": {"name": "Bug"},
                "priority": {"name": priority},
                "security": {"name": SECURITY_LEVEL},
                "labels": ["trivy", "security"]
            }
        }

        try:
            response = self.session.post(
                f"{JIRA_URL}/rest/api/2/issue",
                json=payload,
                timeout=30
            )
            response.raise_for_status()
            return response.json()['key']
        except requests.exceptions.HTTPError as e:
            logging.error(f"Failed to create issue (HTTP {e.response.status_code}): {e.response.text}")
        except Exception as e:
            logging.error(f"Failed to create issue: {str(e)}")
        return None

    def update_existing_issue(self, issue_key: str, vulnerabilities: List[Dict]) -> bool:

        try:

            issue_url = f"{JIRA_URL}/rest/api/2/issue/{issue_key}"
            response = self.session.get(issue_url, timeout=30)
            response.raise_for_status()
            issue_data = response.json()
            
            current_desc = issue_data["fields"]["description"]
            current_labels = issue_data["fields"].get("labels", [])
            

            new_cves = [v for v in vulnerabilities if v['VulnerabilityID'] not in current_desc]
            
            if not new_cves:
                logging.info(f"No new CVEs to add to {issue_key}")
                return True
                

            new_content = "\n".join(self.format_vulnerability(v) for v in new_cves)
            updated_desc = f"{current_desc}\n{new_content}"
            

            payload = {
                "fields": {
                    "description": updated_desc,
                    "labels": list(set(current_labels + ["trivy"]))
                }
            }
            
            # Send update
            response = self.session.put(
                issue_url,
                json=payload,
                timeout=30
            )
            response.raise_for_status()
            logging.info(f"Updated {issue_key} with {len(new_cves)} new CVEs")
            return True
            
        except Exception as e:
            logging.error(f"Failed to update {issue_key}: {str(e)}")
            return False

    def format_vulnerability(self, vulnerability: Dict) -> str:
        return (
            f"* *{vulnerability['VulnerabilityID']}* - {vulnerability['Severity']}\n"
            f"  *Status:* {vulnerability['Status']}\n"
            f"  *Installed Version:* {vulnerability['InstalledVersion']}\n"
            f"  *Fixed Version:* {vulnerability['FixedVersion'] or 'None'}\n"
            f"  *Target:* {vulnerability['Target']}\n"
        )

    def parse_vulnerabilities(self, file_path: str) -> List[Dict]:
        vulnerabilities = []
        try:
            with open(file_path, 'r') as file:
                lines = file.readlines()[2:]  # Skip header
                for line in lines:
                    if line.strip():
                        fields = re.split(r'\s*\|\s*', line.strip())
                        if len(fields) >= 10:
                            vulnerabilities.append({
                                'VulnerabilityID': fields[0].strip(),
                                'Severity': fields[1].strip(),
                                'Status': fields[2].strip(),
                                'InstalledVersion': fields[3].strip(),
                                'FixedVersion': fields[4].strip(),
                                'Class': fields[5].strip(),
                                'Target': fields[6].strip(),
                                'PkgName': fields[7].strip(),
                                'PkgPath': fields[8].strip(),
                                'Title': fields[9].strip()
                            })
        except Exception as e:
            logging.error(f"Error parsing {file_path}: {str(e)}")
        return vulnerabilities

    def process_vulnerabilities(self, vulnerabilities: List[Dict]):
        packages = {}
        for vuln in vulnerabilities:
            pkg_name = vuln['PkgName']
            normalized_name = self.normalize_package_name(pkg_name)
            
            if normalized_name not in packages:
                packages[normalized_name] = []
            packages[normalized_name].append(vuln)
        
        # Process each package group
        for norm_pkg, vulns in packages.items():
            original_pkg_name = vulns[0]['PkgName']
            
            if norm_pkg in self.processed_packages:
                logging.debug(f"Skipping already processed package: {norm_pkg}")
                continue
                
            if self.is_blocked(original_pkg_name, vulns) or self.is_blocked(norm_pkg, vulns):
                logging.info(f"Skipping blocked package: {original_pkg_name}")
                continue
            
            cve_ids = [v['VulnerabilityID'] for v in vulns]
            existing_issue = self.find_existing_issue(norm_pkg, cve_ids)
            
            if existing_issue:
                issue_key = existing_issue['key']
                if issue_key not in self.processed_issues:
                    logging.info(f"Found existing issue {issue_key} for {original_pkg_name}")
                    self.update_existing_issue(issue_key, vulns)
                    self.processed_issues.add(issue_key)
                else:
                    logging.debug(f"Issue {issue_key} already processed")
            else:
                issue_key = self.create_issue(original_pkg_name, vulns)
                if issue_key:
                    logging.info(f"Created new issue {issue_key} for {original_pkg_name}")
                    self.processed_issues.add(issue_key)
            
            self.processed_packages.add(norm_pkg)

def main():

    try:
        integrator = JiraIntegrator()
        vulnerabilities = integrator.parse_vulnerabilities('filtered_vulnerabilities.txt')
        
        if not vulnerabilities:
            logging.warning("No vulnerabilities found to process")
            return
        
        logging.info(f"Processing {len(vulnerabilities)} vulnerabilities")
        integrator.process_vulnerabilities(vulnerabilities)
        logging.info("Processing completed successfully")
        
    except Exception as e:
        logging.error(f"Fatal error in main execution: {str(e)}", exc_info=True)

if __name__ == "__main__":
    main()