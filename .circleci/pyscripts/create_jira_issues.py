import requests
import os
import re
import json

PROJECT_KEY = "NMS"
JIRA_USER = os.getenv("JIRA_USER")
JIRA_API_TOKEN = os.getenv("JIRA_API_TOKEN")
JIRA_URL = os.getenv("JIRA_URL")

# Priority mapping based on severity
PRIORITY_MAP = {
    "Critical": "Critical",
    "Blocker": "Blocker",
    "Major": "Major",
    "Minor": "Minor",
    "Trivial": "Trivial",
    "Highest": "Highest",
    "High": "High",
    "Medium": "Medium",
    "Low": "Low",
    "Lowest": "Lowest"
}

def parse_filtered_vulnerabilities(file_path):
    vulnerabilities = []

    with open(file_path, 'r') as file:
        lines = file.readlines()[2:]

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

def issue_exists(vulnerability_id):
    url = f"{JIRA_URL}/rest/api/2/search?jql=summary~'{vulnerability_id}' AND project='{PROJECT_KEY}'"
    response = requests.get(url, auth=(JIRA_USER, JIRA_API_TOKEN))
    if response.status_code != 200:
        print(f"Error fetching issues: {response.status_code} - {response.text}")
        return False
    return response.json().get('total', 0) > 0

def create_issue(vulnerability):
    priority_name = PRIORITY_MAP.get(vulnerability['Severity'], "Trivial")  # Default to "Trivial"
    
    issue_payload = {
        "fields": {
            "project": {
                "key": PROJECT_KEY
            },
            "summary": f"Trivy Bug: (Vuln ID: {vulnerability['VulnerabilityID']}): {vulnerability['Title']}",
            "description": (
                f"**Vulnerability ID:** {vulnerability['VulnerabilityID']}\n"
                f"**Severity:** {vulnerability['Severity']}\n"
                f"**Status:** {vulnerability['Status']}\n"
                f"**Installed Version:** {vulnerability['InstalledVersion']}\n"
                f"**Fixed Version:** {vulnerability['FixedVersion']}\n"
                f"**Class:** {vulnerability['Class']}\n"
                f"**Target:** {vulnerability['Target']}\n"
                f"**Package Name:** {vulnerability['PkgName']}\n"
                f"**Package Path:** {vulnerability['PkgPath']}\n"
                f"**Title:** {vulnerability['Title']}"
            ),
            "issuetype": {
                "name": "Bug"
            },
            "priority": {
                "name": priority_name  # Use mapped priority
            }
        }
    }
    
    print(f"Posting to URL: {JIRA_URL}/rest/api/2/issue")
    print(f"Payload: {json.dumps(issue_payload, indent=2)}")
    
    response = requests.post(f"{JIRA_URL}/rest/api/2/issue", auth=(JIRA_USER, JIRA_API_TOKEN), 
                             headers={"Content-Type": "application/json"}, 
                             data=json.dumps(issue_payload))
    
    if response.status_code == 201:
        print(f"Created issue: {response.json().get('key')}")
    else:
        print(f"Failed to create issue: {response.status_code} - {response.text}")

def main():
    vulnerabilities = parse_filtered_vulnerabilities('filtered_vulnerabilities.txt')
    
    for vulnerability in vulnerabilities:
        if not issue_exists(vulnerability['VulnerabilityID']):
            create_issue(vulnerability)
        else:
            print(f"Issue for vulnerability {vulnerability['VulnerabilityID']} already exists.")

if __name__ == "__main__":
    main()
