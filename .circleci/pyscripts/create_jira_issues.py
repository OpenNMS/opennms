import requests
import os
import json
import logging
import re
import urllib.parse

PROJECT_KEY = "NMS"
JIRA_USER = os.getenv("JIRA_USER")
JIRA_API_TOKEN = os.getenv("JIRA_API_TOKEN")
JIRA_URL = os.getenv("JIRA_URL")

# Priority mapping for Trivy severity levels
PRIORITY_MAP = {
    "CRITICAL": "Critical",
    "HIGH": "High",
    "MEDIUM": "Medium",
    "LOW": "Low",
    "Trivial": "Trivial"
}

SECURITY_LEVEL = "TOG (migrated)"

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

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
    "java-17-openjdk-headless",
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
    "CVE-2025-30749",
    "CVE-2025-50059",
    "CVE-2025-50106"
}

processed_packages = set()
processed_issues = set()

def normalize_package_name(pkg_name):


    pkg_name = re.sub(r'[-_]\d+.*$', '', pkg_name)

    pkg_name = re.sub(r'[:@/]', '-', pkg_name)
    return pkg_name.lower().strip()

def parse_filtered_vulnerabilities(file_path):
    vulnerabilities = []

    try:
        with open(file_path, 'r') as file:
            lines = file.readlines()[2:]  # Skip the first two lines (header and separator)
    except FileNotFoundError:
        logging.error(f"File {file_path} not found.")
        return vulnerabilities
    except Exception as e:
        logging.error(f"Error reading file {file_path}: {e}")
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

def issue_exists_for_package_and_cves(package_name, vulnerability_ids):

    normalized_pkg = normalize_package_name(package_name)

    cve_conditions = []
    for vuln_id in vulnerability_ids:
        cve_conditions.append(f'text ~ "{vuln_id}"')
    
    jql = (
        f'project = {PROJECT_KEY} AND '
        f'(summary ~ "{normalized_pkg}" OR description ~ "{normalized_pkg}") AND '
        f'({" OR ".join(cve_conditions)}) AND '
        f'resolution IS EMPTY'
    )
    
    try:
        response = requests.get(
            f"{JIRA_URL}/rest/api/2/search",
            params={'jql': jql, 'maxResults': 1},
            auth=(JIRA_USER, JIRA_API_TOKEN)
        )
        response.raise_for_status()
        issues = response.json().get('issues', [])
        return issues[0] if issues else None
    except requests.exceptions.RequestException as e:
        logging.error(f"Error searching for existing issues: {e}")
        logging.debug(f"JQL query that failed: {jql}")
        return None

def add_cves_to_existing_issue(issue_key, vulnerabilities):
    issue_url = f"{JIRA_URL}/rest/api/2/issue/{issue_key}"

    try:
        response = requests.get(issue_url, auth=(JIRA_USER, JIRA_API_TOKEN))
        response.raise_for_status()
        issue_data = response.json()
        current_description = issue_data["fields"]["description"]
        current_labels = issue_data["fields"]["labels"]
    except requests.exceptions.RequestException as e:
        logging.error(f"Error fetching issue details for {issue_key}: {e}")
        return

    new_cves_text = "\n".join([format_vulnerability_details(v) for v in vulnerabilities])
    new_cve_ids = [v['VulnerabilityID'] for v in vulnerabilities]

    if any(cve in current_description for cve in new_cve_ids):
        logging.info(f"All CVEs already exist in issue {issue_key}. Skipping update.")
        return

    updated_description = current_description + "\n" + new_cves_text
    if "trivy" not in current_labels:
        current_labels.append("trivy")

    update_payload = {
        "fields": {
            "description": updated_description,
            "labels": current_labels
        }
    }

    try:
        response = requests.put(issue_url, auth=(JIRA_USER, JIRA_API_TOKEN),
                               headers={"Content-Type": "application/json"},
                               data=json.dumps(update_payload))
        response.raise_for_status()
        logging.info(f"Updated issue {issue_key} with new CVEs")
    except requests.exceptions.RequestException as e:
        logging.error(f"Failed to update issue {issue_key}: {e}")

def format_vulnerability_details(vulnerability):
    return (
        f"*Vulnerability ID:* {vulnerability['VulnerabilityID']}\n"
        f"*Severity:* {vulnerability['Severity']}\n"
        f"*Status:* {vulnerability['Status']}\n"
        f"*Installed Version:* {vulnerability['InstalledVersion']}\n"
        f"*Fixed Version:* {vulnerability['FixedVersion']}\n"
        f"*Class:* {vulnerability['Class']}\n"
        f"*Target:* {vulnerability['Target']}\n"
        f"*Package Path:* {vulnerability['PkgPath']}\n"
        f"*Title:* {vulnerability['Title']}\n\n"
    )

def extract_component_from_target(target):
    components = ["minion", "sentinel", "horizon"]
    for component in components:
        if component in target:
            return component
    return None

def create_issue_for_package(package_name, vulnerabilities):
    severity_levels = set([v['Severity'] for v in vulnerabilities])
    priority_name = "Trivial"
    if "CRITICAL" in severity_levels:
        priority_name = "Critical"
    elif "HIGH" in severity_levels:
        priority_name = "High"
    elif "MEDIUM" in severity_levels:
        priority_name = "Medium"
    elif "LOW" in severity_levels:
        priority_name = "Low"

    component = extract_component_from_target(vulnerabilities[0]['Target'])
    component_prefix = f"[{component}] " if component else ""

    summary = f"{component_prefix}Trivy Bug: Vulnerabilities in {package_name}"
    vulnerabilities_list = "\n".join([format_vulnerability_details(v) for v in vulnerabilities])
    description = (
        f"**Package Name:** {package_name}\n\n"
        f"**List of CVEs:**\n"
        f"{vulnerabilities_list}"
    )

    issue_payload = {
        "fields": {
            "project": {
                "key": PROJECT_KEY
            },
            "summary": summary,
            "description": description,
            "issuetype": {
                "name": "Bug"
            },
            "priority": {
                "name": priority_name
            },
            "security": {
                "name": SECURITY_LEVEL
            },
            "labels": ["trivy"]
        }
    }

    try:
        response = requests.post(f"{JIRA_URL}/rest/api/2/issue", auth=(JIRA_USER, JIRA_API_TOKEN),
                                 headers={"Content-Type": "application/json"},
                                 data=json.dumps(issue_payload))
        response.raise_for_status()
        created_issue_key = response.json().get('key')
        processed_issues.add(created_issue_key)
        logging.info(f"Created issue: {created_issue_key}")
        return created_issue_key
    except requests.exceptions.RequestException as e:
        logging.error(f"Failed to create issue: {e}")
        return None

def create_issues(vulnerabilities):
    packages = {}
    for vulnerability in vulnerabilities:
        pkg_name = vulnerability['PkgName']
        if pkg_name not in packages:
            packages[pkg_name] = []
        packages[pkg_name].append(vulnerability)

    for package_name, package_vulnerabilities in packages.items():
        if package_name in processed_packages:
            logging.info(f"Package {package_name} already processed. Skipping.")
            continue

        if package_name in BLOCKLIST or any(v['VulnerabilityID'] in BLOCKLIST for v in package_vulnerabilities):
            logging.info(f"Package {package_name} or its vulnerabilities are in the blocklist. Skipping.")
            continue

        vulnerability_ids = [v['VulnerabilityID'] for v in package_vulnerabilities]
        existing_issue = issue_exists_for_package_and_cves(package_name, vulnerability_ids)

        if existing_issue:
            issue_key = existing_issue["key"]
            logging.info(f"Issue for {package_name} exists: {issue_key}")

            if issue_key not in processed_issues:
                add_cves_to_existing_issue(issue_key, package_vulnerabilities)
                processed_issues.add(issue_key)
            else:
                logging.info(f"Issue {issue_key} already processed. Skipping.")
        else:
            logging.info(f"Issue for {package_name} does not exist. Creating issue.")
            created_issue = create_issue_for_package(package_name, package_vulnerabilities)
            if created_issue:
                processed_issues.add(created_issue)

        processed_packages.add(package_name)

def main():
    vulnerabilities = parse_filtered_vulnerabilities('filtered_vulnerabilities.txt')

    if not vulnerabilities:
        logging.info("No vulnerabilities to process.")
        return

    create_issues(vulnerabilities)

if __name__ == "__main__":
    main()