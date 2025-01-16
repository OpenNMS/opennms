import requests
import os
import re
import json
import logging
from collections import defaultdict

PROJECT_KEY = "NMS"
JIRA_USER = os.getenv("JIRA_USER")
JIRA_API_TOKEN = os.getenv("JIRA_API_TOKEN")
JIRA_URL = os.getenv("JIRA_URL")

# Priority mapping based on severity
PRIORITY_MAP = {
    "CRITICAL": "Critical",
    "HIGH": "High",
    "MEDIUM": "Medium",
    "LOW": "Low",
    "Trivial": "Trivial"
}

def parse_filtered_vulnerabilities(file_path):
    vulnerabilities = []

    try:
        with open(file_path, 'r') as file:
            lines = file.readlines()[2:]
    except FileNotFoundError:
        print(f"File {file_path} not found.")
        return vulnerabilities
    except Exception as e:
        print(f"Error reading file {file_path}: {e}")
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

logging.basicConfig(level=logging.INFO)

def issue_exists(vulnerability_id):
    url = f"{JIRA_URL}/rest/api/2/search?jql=summary~'{vulnerability_id}' AND project='{PROJECT_KEY}'"
    try:
        response = requests.get(url, auth=(JIRA_USER, JIRA_API_TOKEN))
        response.raise_for_status()
    except requests.exceptions.RequestException as e:
        logging.error(f"Error fetching issues for {vulnerability_id}: {e}")
        return False
    return response.json().get('total', 0) > 0

def create_issue(pkg_name, vulnerabilities):

    vuln_details = []
    for vulnerability in vulnerabilities:
        vuln_details.append(f"**Vulnerability ID:** {vulnerability['VulnerabilityID']}\n"
                            f"**Severity:** {vulnerability['Severity']}\n"
                            f"**Status:** {vulnerability['Status']}\n"
                            f"**Installed Version:** {vulnerability['InstalledVersion']}\n"
                            f"**Fixed Version:** {vulnerability['FixedVersion']}\n"
                            f"**Class:** {vulnerability['Class']}\n"
                            f"**Target:** {vulnerability['Target']}\n"
                            f"**Package Path:** {vulnerability['PkgPath']}\n"
                            f"**Title:** {vulnerability['Title']}\n\n")

    vulnerability_summary = "\n".join(vuln_details)
    
    # Get the priority based on severity
    priority_name = PRIORITY_MAP.get(vulnerabilities[0]['Severity'], "Trivial")  # Default to "Trivial"
    
    # Create the Jira issue payload
    issue_payload = {
        "fields": {
            "project": {
                "key": PROJECT_KEY
            },
            "summary": f"Trivy Bug: Library {pkg_name}",
            "description": (
                f"**Package Name:** {pkg_name}\n\n"
                f"**List of CVEs:**\n"
                f"{vulnerability_summary}"
            ),
            "issuetype": {
                "name": "Bug"
            },
            "priority": {
                "name": priority_name
            }
        }
    }

    try:
        print(f"Posting to URL: {JIRA_URL}/rest/api/2/issue")
        response = requests.post(f"{JIRA_URL}/rest/api/2/issue", auth=(JIRA_USER, JIRA_API_TOKEN), 
                                 headers={"Content-Type": "application/json"}, 
                                 data=json.dumps(issue_payload))
        response.raise_for_status()
        print(f"Created issue for library {pkg_name}: {response.json().get('key')}")
    except requests.exceptions.RequestException as e:
        print(f"Failed to create issue for {pkg_name}: {e}")

def main():
    # Parse vulnerabilities from the filtered file
    vulnerabilities = parse_filtered_vulnerabilities('filtered_vulnerabilities.txt')
    
    if not vulnerabilities:
        print("No vulnerabilities to process.")
        return
    
    # Group vulnerabilities by package name (PkgName)
    grouped_vulnerabilities = defaultdict(list)
    
    for vulnerability in vulnerabilities:
        grouped_vulnerabilities[vulnerability['PkgName']].append(vulnerability)
    

    for pkg_name, vuln_list in grouped_vulnerabilities.items():
        if vuln_list:
            if not issue_exists(pkg_name): 
                create_issue(pkg_name, vuln_list)
            else:
                print(f"Issue for library {pkg_name} already exists.")

if __name__ == "__main__":
    main()
