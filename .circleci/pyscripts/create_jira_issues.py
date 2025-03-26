import requests
import os
import json
import logging
import re
import urllib.parse
from typing import List, Dict, Optional

# Configuration
PROJECT_KEY = "NMS"
JIRA_USER = os.getenv("JIRA_USER")
JIRA_API_TOKEN = os.getenv("JIRA_API_TOKEN")
JIRA_URL = os.getenv("JIRA_URL")

# Security configuration
SECURITY_LEVEL = "TOG (migrated)"

# Priority mapping
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
    "CVE-2024-12797",
    "CVE-2024-56171",
    "CVE-2023-39325",
    "CVE-2024-45338",
    "CVE-2024-2961", 
    "CVE-2020-11971",
}

# Logging configuration
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[
        logging.FileHandler("jira_integration.log"),
        logging.StreamHandler()
    ]
)

# Global state tracking
processed_packages = set()
processed_issues = set()
package_name_mappings = {}

def load_package_mappings(file_path: str = "package_mappings.txt") -> Dict[str, List[str]]:
    """Load package name mappings from file"""
    mappings = {}
    try:
        with open(file_path, 'r') as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith('#'):
                    parts = [p.strip() for p in line.split('=')]
                    if len(parts) == 2:
                        canonical, variants = parts
                        mappings[canonical] = [v.strip() for v in variants.split(',')]
        logging.info(f"Loaded {len(mappings)} package mappings")
    except FileNotFoundError:
        logging.warning(f"No package mappings file found at {file_path}")
    except Exception as e:
        logging.error(f"Error loading package mappings: {e}")
    return mappings

def normalize_package_name(pkg_name: str) -> str:
    """Normalize package names for consistent matching"""
    pkg_name = pkg_name.lower().strip()
    
    # Check if this package has known variations
    for canonical_name, variants in package_name_mappings.items():
        if pkg_name == canonical_name or pkg_name in variants:
            return canonical_name
    
    # Remove version numbers if present
    pkg_name = re.sub(r'[-_]\d+.*$', '', pkg_name)
    return pkg_name

def parse_filtered_vulnerabilities(file_path: str) -> List[Dict]:
    """Parse vulnerabilities from the input file"""
    vulnerabilities = []
    
    try:
        with open(file_path, 'r') as file:
            lines = file.readlines()[2:]  # Skip header
    except Exception as e:
        logging.error(f"Error reading {file_path}: {e}")
        return vulnerabilities

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
    
    return vulnerabilities

def find_existing_issue(package_name: str, vulnerability_ids: List[str]) -> Optional[Dict]:
    """Find existing Jira issue for this package and CVEs"""
    normalized_pkg = normalize_package_name(package_name)
    
    # Build comprehensive search patterns for CVEs
    cve_patterns = []
    for vuln_id in vulnerability_ids:
        cve_patterns.append(f'description ~ "\\\\"{vuln_id}\\\\""')  # Exact match in quotes
        cve_patterns.append(f'description ~ "{vuln_id}[^a-zA-Z0-9]"')  # CVE followed by non-alnum
        cve_patterns.append(f'description ~ "[^a-zA-Z0-9]{vuln_id}"')  # CVE preceded by non-alnum
    
    jql = (
        f'project = {PROJECT_KEY} AND '
        f'(summary ~ "{normalized_pkg}" OR description ~ "{normalized_pkg}") AND '
        f'({" OR ".join(cve_patterns)}) AND '
        f'resolution IS EMPTY'  # Only open issues
    )
    
    try:
        response = requests.get(
            f"{JIRA_URL}/rest/api/2/search",
            params={'jql': jql, 'maxResults': 1},
            auth=(JIRA_USER, JIRA_API_TOKEN),
            timeout=30
        )
        response.raise_for_status()
        issues = response.json().get('issues', [])
        return issues[0] if issues else None
    except Exception as e:
        logging.error(f"Error searching for existing issues: {e}")
        return None

def update_existing_issue(issue_key: str, vulnerabilities: List[Dict]) -> bool:
    """Add new CVEs to an existing issue"""
    try:
        # Get current issue data
        issue_url = f"{JIRA_URL}/rest/api/2/issue/{issue_key}"
        response = requests.get(issue_url, auth=(JIRA_USER, JIRA_API_TOKEN))
        response.raise_for_status()
        issue_data = response.json()
        
        current_desc = issue_data["fields"]["description"]
        current_labels = issue_data["fields"].get("labels", [])
        
        # Check if any CVEs are already present
        new_cves = []
        for vuln in vulnerabilities:
            if vuln['VulnerabilityID'] not in current_desc:
                new_cves.append(vuln)
        
        if not new_cves:
            logging.info(f"All CVEs already exist in {issue_key}")
            return True
            
        # Format new CVEs
        new_content = "\n".join(format_vulnerability_details(v) for v in new_cves)
        updated_desc = f"{current_desc}\n{new_content}"
        
        # Prepare update payload
        payload = {
            "fields": {
                "description": updated_desc,
                "labels": list(set(current_labels + ["trivy"]))
            }
        }
        
        # Send update
        response = requests.put(
            issue_url,
            auth=(JIRA_USER, JIRA_API_TOKEN),
            headers={"Content-Type": "application/json"},
            data=json.dumps(payload),
            timeout=30
        )
        response.raise_for_status()
        logging.info(f"Updated {issue_key} with {len(new_cves)} new CVEs")
        return True
        
    except Exception as e:
        logging.error(f"Failed to update {issue_key}: {e}")
        return False

def format_vulnerability_details(vulnerability: Dict) -> str:
    """Format vulnerability details for Jira description"""
    return (
        f"*Vulnerability ID:* {vulnerability['VulnerabilityID']}\n"
        f"*Severity:* {vulnerability['Severity']}\n"
        f"*Status:* {vulnerability['Status']}\n"
        f"*Installed Version:* {vulnerability['InstalledVersion']}\n"
        f"*Fixed Version:* {vulnerability['FixedVersion']}\n"
        f"*Class:* {vulnerability['Class']}\n"
        f"*Target:* {vulnerability['Target']}\n"
        f"*Package Path:* {vulnerability['PkgPath']}\n"
        f"*Title:* {vulnerability['Title']}\n"
    )

def create_new_issue(package_name: str, vulnerabilities: List[Dict]) -> Optional[str]:
    """Create a new Jira issue for these vulnerabilities"""
    # Determine priority
    severities = {v['Severity'] for v in vulnerabilities}
    priority = "Trivial"
    if "CRITICAL" in severities:
        priority = "Critical"
    elif "HIGH" in severities:
        priority = "High"
    elif "MEDIUM" in severities:
        priority = "Medium"
    elif "LOW" in severities:
        priority = "Low"
    
    # Format description
    vuln_details = "\n".join(format_vulnerability_details(v) for v in vulnerabilities)
    description = (
        f"**Package Name:** {package_name}\n\n"
        f"**List of CVEs:**\n{vuln_details}"
    )
    
    # Prepare payload
    payload = {
        "fields": {
            "project": {"key": PROJECT_KEY},
            "summary": f"Trivy Bug: Vulnerabilities in {package_name}",
            "description": description,
            "issuetype": {"name": "Bug"},
            "priority": {"name": priority},
            "security": {"name": SECURITY_LEVEL},
            "labels": ["trivy"]
        }
    }
    
    try:
        response = requests.post(
            f"{JIRA_URL}/rest/api/2/issue",
            auth=(JIRA_USER, JIRA_API_TOKEN),
            headers={"Content-Type": "application/json"},
            data=json.dumps(payload),
            timeout=30
        )
        response.raise_for_status()
        issue_key = response.json()['key']
        logging.info(f"Created new issue {issue_key} for {package_name}")
        return issue_key
    except Exception as e:
        logging.error(f"Failed to create issue for {package_name}: {e}")
        return None

def process_vulnerabilities(vulnerabilities: List[Dict]):
    """Process all vulnerabilities and create/update Jira issues"""
    # Group by normalized package name
    packages = {}
    for vuln in vulnerabilities:
        pkg_name = normalize_package_name(vuln['PkgName'])
        if pkg_name not in packages:
            packages[pkg_name] = []
        packages[pkg_name].append(vuln)
    
    # Process each package
    for pkg_name, vulns in packages.items():
        if pkg_name in processed_packages:
            continue
            
        if pkg_name in BLOCKLIST or any(v['VulnerabilityID'] in BLOCKLIST for v in vulns):
            logging.info(f"Skipping blocked package: {pkg_name}")
            continue
        
        # Check for existing issue
        cve_ids = [v['VulnerabilityID'] for v in vulns]
        existing_issue = find_existing_issue(pkg_name, cve_ids)
        
        if existing_issue:
            issue_key = existing_issue['key']
            if issue_key not in processed_issues:
                update_existing_issue(issue_key, vulns)
                processed_issues.add(issue_key)
        else:
            created_issue = create_new_issue(pkg_name, vulns)
            if created_issue:
                processed_issues.add(created_issue)
        
        processed_packages.add(pkg_name)

def main():
    """Main execution function"""
    global package_name_mappings
    
    try:
        # Load package mappings first
        package_name_mappings = load_package_mappings()
        
        # Then process vulnerabilities
        vulns = parse_filtered_vulnerabilities('filtered_vulnerabilities.txt')
        if not vulns:
            logging.info("No vulnerabilities to process")
            return
        
        logging.info(f"Processing {len(vulns)} vulnerabilities")
        process_vulnerabilities(vulns)
        logging.info("Processing completed")
        
    except Exception as e:
        logging.error(f"Fatal error in main execution: {e}", exc_info=True)

if __name__ == "__main__":
    main()