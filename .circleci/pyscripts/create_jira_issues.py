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

PRIORITY_MAP = {
    "CRITICAL": "Critical",
    "HIGH": "High",
    "MEDIUM": "Medium",
    "LOW": "Low",
    "Trivial": "Trivial"
}

# Security level for Trivy issues
SECURITY_LEVEL = "TOG (migrated)"

logging.basicConfig(level=logging.INFO)

def parse_filtered_vulnerabilities(file_path):
    vulnerabilities = []

    try:
        with open(file_path, 'r') as file:
            lines = file.readlines()[2:]
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

def issue_exists_for_vulnerability(package_name, vulnerability_id):
    jql = f'project="{PROJECT_KEY}" AND summary ~ "{package_name}" AND description ~ "{vulnerability_id}" AND resolution not in ("Won\'t Fix", "Not a Bug") AND status not in ("Closed", "Resolved")'
    jql = urllib.parse.quote(jql)
    decoded_jql = urllib.parse.unquote(jql)
    logging.info(f"JQL Query: {decoded_jql}")

    url = f"{JIRA_URL}/rest/api/2/search?jql={jql}"

    try:
        response = requests.get(url, auth=(JIRA_USER, JIRA_API_TOKEN))
        response.raise_for_status()
    except requests.exceptions.RequestException as e:
        logging.error(f"Error fetching issues for {package_name} and {vulnerability_id}: {e}")
        return None

    issues = response.json().get('issues', [])
    return issues[0] if issues else None

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
        logging.info(f"Some CVEs are already listed in issue {issue_key}. Skipping update for those.")
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

    summary = f"Trivy Bug: Vulnerabilities in {package_name}"
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
        logging.info(f"Posting to URL: {JIRA_URL}/rest/api/2/issue")
        response = requests.post(f"{JIRA_URL}/rest/api/2/issue", auth=(JIRA_USER, JIRA_API_TOKEN),
                                 headers={"Content-Type": "application/json"},
                                 data=json.dumps(issue_payload))
        response.raise_for_status()
        logging.info(f"Created issue: {response.json().get('key')}")
    except requests.exceptions.RequestException as e:
        logging.error(f"Failed to create issue: {e}")

def create_issues(vulnerabilities):
    packages = {}
    for vulnerability in vulnerabilities:
        pkg_name = vulnerability['PkgName']
        if pkg_name not in packages:
            packages[pkg_name] = []
        packages[pkg_name].append(vulnerability)

    for package_name, package_vulnerabilities in packages.items():
        vulnerability_ids = [v['VulnerabilityID'] for v in package_vulnerabilities]
        existing_issue = issue_exists_for_vulnerability(package_name, vulnerability_ids[0])

        if existing_issue:
            issue_key = existing_issue["key"]
            logging.info(f"Issue for {package_name} exists: {issue_key}")
            add_cves_to_existing_issue(issue_key, package_vulnerabilities)
        else:
            logging.info(f"Issue for {package_name} does not exist. Creating issue.")
            create_issue_for_package(package_name, package_vulnerabilities)

def main():
    vulnerabilities = parse_filtered_vulnerabilities('filtered_vulnerabilities.txt')

    if not vulnerabilities:
        logging.info("No vulnerabilities to process.")
        return

    create_issues(vulnerabilities)

if __name__ == "__main__":
    main()
