import requests
import os
import json
import logging
import re
import argparse

PROJECT_KEY = "NMS"
EPIC_KEY = "NMS-16937" 
EPIC_LINK_FIELD = "customfield_10014" 
JIRA_USER = os.getenv("JIRA_USER")
JIRA_API_TOKEN = os.getenv("JIRA_API_TOKEN")
JIRA_URL = os.getenv("JIRA_URL")

# Dry-run mode flag - when True, logs verbose details and skips API calls
DRY_RUN = False

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

def validate_environment():
    """Validate required environment variables are set."""
    missing = []
    if not JIRA_USER:
        missing.append("JIRA_USER")
    if not JIRA_API_TOKEN:
        missing.append("JIRA_API_TOKEN")
    if not JIRA_URL:
        missing.append("JIRA_URL")
    
    if missing:
        logging.error(f"Missing required environment variables: {', '.join(missing)}")
        return False
    return True

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

def load_vulnerabilities_by_package(file_path):
    """
    Load the JSON file and consolidate packages by PkgName only.
    This merges the same library found at different paths into one entry.
    """
    try:
        with open(file_path, 'r') as f:
            packages = json.load(f)
    except FileNotFoundError:
        logging.error(f"File {file_path} not found.")
        return {}
    except json.JSONDecodeError as e:
        logging.error(f"Error parsing JSON file {file_path}: {e}")
        return {}

    # Consolidate by PkgName (merge different paths)
    consolidated = {}
    for pkg in packages:
        pkg_name = pkg['PkgName']
        
        if pkg_name not in consolidated:
            consolidated[pkg_name] = {
                'PkgName': pkg_name,
                'Paths': [],
                'InstalledVersions': set(),
                'Products': set(),
                'Vulnerabilities': [],
                'HighestSeverity': 'LOW',
                'FixedVersions': set(),
                'CriticalCount': 0,
                'HighCount': 0,
                'Class': pkg.get('Class', ''),
                'Target': pkg.get('Target', '')
            }
        
        entry = consolidated[pkg_name]
        entry['Paths'].append(pkg['PkgPath'])
        entry['InstalledVersions'].add(pkg['InstalledVersion'])
        entry['Products'].update(pkg.get('Products', []))
        
        # Merge vulnerabilities (deduplicate by VulnerabilityID)
        existing_vuln_ids = {v['VulnerabilityID'] for v in entry['Vulnerabilities']}
        for vuln in pkg.get('Vulnerabilities', []):
            if vuln['VulnerabilityID'] not in existing_vuln_ids:
                entry['Vulnerabilities'].append(vuln)
                existing_vuln_ids.add(vuln['VulnerabilityID'])
        
        # Update severity counts
        entry['CriticalCount'] += pkg.get('CriticalCount', 0)
        entry['HighCount'] += pkg.get('HighCount', 0)
        
        # Track highest severity
        severity_order = {'CRITICAL': 4, 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1}
        if severity_order.get(pkg.get('HighestSeverity', 'LOW'), 0) > severity_order.get(entry['HighestSeverity'], 0):
            entry['HighestSeverity'] = pkg['HighestSeverity']
        
        # Collect fixed versions
        for fv in pkg.get('FixedVersions', []):
            if fv and fv != 'N/A':
                entry['FixedVersions'].add(fv)
    
    # Convert sets to lists for JSON compatibility
    for pkg_name, entry in consolidated.items():
        entry['InstalledVersions'] = sorted(list(entry['InstalledVersions']))
        entry['Products'] = sorted(list(entry['Products']))
        entry['FixedVersions'] = sorted(list(entry['FixedVersions']))
        entry['VulnerabilityCount'] = len(entry['Vulnerabilities'])
    
    return consolidated

def parse_filtered_vulnerabilities(file_path):
    """Legacy function for backward compatibility with text file format."""
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
    """Check if a Jira issue exists for the given package and CVEs."""
    if not vulnerability_ids:
        return None
    
    normalized_pkg = normalize_package_name(package_name)

    cve_conditions = []
    for vuln_id in vulnerability_ids:
        cve_conditions.append(f'text ~ "{vuln_id}"')
    
    jql = (
        f'project = {PROJECT_KEY} AND '
        f'labels = trivy AND '
        f'(summary ~ "{normalized_pkg}" OR description ~ "{normalized_pkg}") AND '
        f'({" OR ".join(cve_conditions)}) AND '
        f'resolution IS EMPTY'
    )
    
    try:
        response = requests.get(
            f"{JIRA_URL}/rest/api/3/search/jql",
            params={'jql': jql, 'maxResults': 1,"fields": ["key", "summary","id"]},
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
    """Add missing CVEs to an existing Jira issue. Returns True if updated, False otherwise."""
    issue_url = f"{JIRA_URL}/rest/api/2/issue/{issue_key}"

    try:
        response = requests.get(issue_url, auth=(JIRA_USER, JIRA_API_TOKEN))
        response.raise_for_status()
        issue_data = response.json()
        current_description = issue_data["fields"].get("description")
        current_labels = issue_data["fields"].get("labels", [])
        status_category = issue_data["fields"]["status"].get("statusCategory", {}).get("key", "")
    except requests.exceptions.RequestException as e:
        logging.error(f"Error fetching issue details for {issue_key}: {e}")
        return False

    # Only update if issue is not in "Done" category (allows To Do, In Progress, etc.)
    # Status categories: "new" (To Do), "indeterminate" (In Progress), "done" (Done)
    if status_category == "done":
        logging.info(f"Issue {issue_key} is in 'Done' status category. Skipping update.")
        return False

    # Handle None description
    if current_description is None:
        current_description = ""

    # Filter out CVEs that already exist in the description
    missing_cves = [v for v in vulnerabilities if v['VulnerabilityID'] not in current_description]

    if not missing_cves:
        logging.info(f"All CVEs already exist in issue {issue_key}. Skipping update.")
        return False

    logging.info(f"Adding {len(missing_cves)} missing CVEs to issue {issue_key}")
    new_cves_text = "\n".join([format_vulnerability_details(v) for v in missing_cves])
    updated_description = current_description + "\n" + new_cves_text
    if "trivy" not in current_labels:
        current_labels.append("trivy")

    update_payload = {
        "fields": {
            "description": updated_description,
            "labels": current_labels
        }
    }

    if DRY_RUN:
        missing_cve_ids = [v['VulnerabilityID'] for v in missing_cves]
        logging.info(f"[DRY-RUN] Would update issue {issue_key} with {len(missing_cves)} CVEs: {', '.join(missing_cve_ids)}")
        return True

    try:
        response = requests.put(issue_url, auth=(JIRA_USER, JIRA_API_TOKEN),
                               headers={"Content-Type": "application/json"},
                               data=json.dumps(update_payload))
        response.raise_for_status()
        logging.info(f"Updated issue {issue_key} with new CVEs")
        return True
    except requests.exceptions.RequestException as e:
        logging.error(f"Failed to update issue {issue_key}: {e}")
        return False

def format_vulnerability_details(vulnerability):
    """Format a single vulnerability for Jira description."""
    return (
        f"*Vulnerability ID:* {vulnerability['VulnerabilityID']}\n"
        f"*Severity:* {vulnerability['Severity']}\n"
        f"*Status:* {vulnerability.get('Status', 'unknown')}\n"
        f"*Fixed Version:* {vulnerability.get('FixedVersion', 'N/A')}\n"
        f"*Title:* {vulnerability.get('Title', 'N/A')}\n\n"
    )

def format_package_details(package):
    """Format a consolidated package entry for Jira description."""
    vuln_list = "\n".join([format_vulnerability_details(v) for v in package['Vulnerabilities']])
    paths_list = "\n".join([f"• {p}" for p in package['Paths'][:10]])  # Limit to 10 paths
    if len(package['Paths']) > 10:
        paths_list += f"\n• ... and {len(package['Paths']) - 10} more paths"
    
    return (
        f"**Package Name:** {package['PkgName']}\n"
        f"**Installed Versions:** {', '.join(package['InstalledVersions'])}\n"
        f"**Recommended Versions:** {', '.join(package['FixedVersions']) or 'N/A'}\n"
        f"**Affected Products:** {', '.join(package['Products'])}\n"
        f"**Vulnerability Count:** {package['VulnerabilityCount']} ({package['CriticalCount']} Critical, {package['HighCount']} High)\n\n"
        f"**Affected Paths:**\n{paths_list}\n\n"
        f"**List of CVEs:**\n{vuln_list}"
    )

def extract_component_from_products(products):
    """Extract component label from products list."""
    components = ["minion", "sentinel", "horizon"]
    found = [c for c in components if c in products]
    if len(found) == 1:
        return found[0]
    elif len(found) > 1:
        return None  # Multiple products, don't prefix
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

    # Extract component from Target field
    target = vulnerabilities[0].get('Target', '')
    component = None
    for comp in ["minion", "sentinel", "horizon"]:
        if comp in target.lower():
            component = comp
            break
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
            "labels": ["trivy"],
            "fixVersions": [
               {
                "name": "Next"
               }
            ],
            
            EPIC_LINK_FIELD: EPIC_KEY  # Link to epic
        }
    }

    if DRY_RUN:
        vuln_ids = [v['VulnerabilityID'] for v in vulnerabilities]
        logging.info(f"[DRY-RUN] Would create issue for {package_name}")
        logging.info(f"[DRY-RUN]   Summary: {summary}")
        logging.info(f"[DRY-RUN]   Priority: {priority_name}")
        logging.info(f"[DRY-RUN]   CVEs: {', '.join(vuln_ids)}")
        return f"DRY-RUN-{package_name}"

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
    """Legacy function for backward compatibility with old text format."""
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

def create_issue_for_consolidated_package(package):
    """Create a Jira issue from a consolidated package entry (from JSON)."""
    pkg_name = package['PkgName']
    
    priority_name = PRIORITY_MAP.get(package['HighestSeverity'], "Trivial")
    
    # Determine component prefix from products
    component = extract_component_from_products(package['Products'])
    component_prefix = f"[{component}] " if component else ""
    
    # Build summary with CVE count
    cve_count = package['VulnerabilityCount']
    summary = f"{component_prefix}Trivy Bug: {cve_count} vulnerabilities in {pkg_name}"
    
    # Build rich description
    description = format_package_details(package)
    
    issue_payload = {
        "fields": {
            "project": {"key": PROJECT_KEY},
            "summary": summary,
            "description": description,
            "issuetype": {"name": "Bug"},
            "priority": {"name": priority_name},
            "security": {"name": SECURITY_LEVEL},
            "labels": ["trivy"] + list(package['Products']),  # Add product labels
            "fixVersions": [{"name": "Next"}],
            EPIC_LINK_FIELD: EPIC_KEY
        }
    }

    if DRY_RUN:
        vuln_ids = [v['VulnerabilityID'] for v in package['Vulnerabilities']]
        logging.info(f"[DRY-RUN] Would create issue for {pkg_name}")
        logging.info(f"[DRY-RUN]   Summary: {summary}")
        logging.info(f"[DRY-RUN]   Priority: {priority_name}")
        logging.info(f"[DRY-RUN]   CVEs ({cve_count}): {', '.join(vuln_ids)}")
        logging.info(f"[DRY-RUN]   Products: {', '.join(package['Products'])}")
        return f"DRY-RUN-{pkg_name}"

    logging.info(f"Creating issue for {pkg_name}: {summary}")

    try:
        response = requests.post(f"{JIRA_URL}/rest/api/2/issue", auth=(JIRA_USER, JIRA_API_TOKEN),
                                 headers={"Content-Type": "application/json"},
                                 data=json.dumps(issue_payload))
        response.raise_for_status()
        created_issue_key = response.json().get('key')
        logging.info(f"Created issue: {created_issue_key}")
        return created_issue_key
    except requests.exceptions.RequestException as e:
        logging.error(f"Failed to create issue for {pkg_name}: {e}")
        return None

def create_issues_from_json(packages_dict):
    """
    Create Jira issues from consolidated packages dictionary.
    This is the preferred method using the new JSON format.
    """
    for pkg_name, package in packages_dict.items():
        if pkg_name in processed_packages:
            logging.info(f"Package {pkg_name} already processed. Skipping.")
            continue

        # Check blocklist for package name
        if pkg_name in BLOCKLIST:
            logging.info(f"Package {pkg_name} is in the blocklist. Skipping.")
            continue

        # Filter out blocked vulnerabilities before any processing
        original_count = len(package['Vulnerabilities'])
        package['Vulnerabilities'] = [v for v in package['Vulnerabilities'] 
                                      if v['VulnerabilityID'] not in BLOCKLIST]
        package['VulnerabilityCount'] = len(package['Vulnerabilities'])
        
        if package['VulnerabilityCount'] == 0:
            logging.info(f"All {original_count} vulnerabilities for {pkg_name} are blocklisted. Skipping.")
            continue
        
        if package['VulnerabilityCount'] < original_count:
            filtered_count = original_count - package['VulnerabilityCount']
            logging.info(f"Filtered {filtered_count} blocklisted CVEs from {pkg_name}")
            
            # Recalculate severity counts after filtering
            package['CriticalCount'] = sum(1 for v in package['Vulnerabilities'] if v['Severity'] == 'CRITICAL')
            package['HighCount'] = sum(1 for v in package['Vulnerabilities'] if v['Severity'] == 'HIGH')
            
            # Recalculate highest severity
            severity_order = {'CRITICAL': 4, 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1}
            severities = [v['Severity'] for v in package['Vulnerabilities']]
            package['HighestSeverity'] = max(severities, key=lambda s: severity_order.get(s, 0)) if severities else 'LOW'

        # Build vulnerability IDs from filtered list
        vuln_ids = [v['VulnerabilityID'] for v in package['Vulnerabilities']]

        # Check for existing issue
        existing_issue = issue_exists_for_package_and_cves(pkg_name, vuln_ids)

        if existing_issue:
            issue_key = existing_issue["key"]
            logging.info(f"Issue for {pkg_name} exists: {issue_key}")
            
            if issue_key not in processed_issues:
                add_cves_to_existing_issue(issue_key, package['Vulnerabilities'])
                processed_issues.add(issue_key)
            else:
                logging.info(f"Issue {issue_key} already processed. Skipping.")
        else:
            logging.info(f"Creating issue for {pkg_name} ({package['VulnerabilityCount']} CVEs)")
            create_issue_for_consolidated_package(package)

        processed_packages.add(pkg_name)

def parse_args():
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(description='Create Jira issues from Trivy vulnerability scan results.')
    parser.add_argument('--dry-run', action='store_true', 
                        help='Run in dry-run mode: logs verbose details including CVE IDs and skips actual API calls')
    return parser.parse_args()

def main():
    global DRY_RUN
    
    args = parse_args()
    DRY_RUN = args.dry_run
    
    if DRY_RUN:
        logging.info("Running in DRY-RUN mode - no changes will be made to Jira")

    # Validate environment variables
    if not validate_environment():
        logging.error("Exiting due to missing environment variables.")
        return

    # Determine paths based on environment (CircleCI vs local)
    circleci_base = '/home/circleci/project'
    
    base_path = circleci_base
    json_path = f'{base_path}/artifacts/vulnerabilities_by_package.json'
    txt_path = f'{base_path}/artifacts/filtered_vulnerabilities.txt'
    blocklist_path = f'{base_path}/.circleci/trivy-config/blocked_list.json'
    
    
    logging.info(f"Using base path: {base_path}")
    
    # If BLOCKLIST file exists load it
    if os.path.exists(blocklist_path):
        try:
            with open(blocklist_path, 'r') as bl_file:
                bl_data = json.load(bl_file)
                BLOCKLIST.update(bl_data)
                logging.info(f"Loaded {len(bl_data)} entries into blocklist from file.")
        except Exception as e:
            logging.error(f"Error loading blocklist file: {e}")
    else:
        logging.info("No blocklist file found, using default blocklist.")


    if os.path.exists(json_path):
        logging.info(f"Loading vulnerabilities from JSON: {json_path}")
        packages = load_vulnerabilities_by_package(json_path)
        
        if not packages:
            logging.info("No packages to process.")
            return
        
        logging.info(f"Loaded {len(packages)} unique packages")
        create_issues_from_json(packages)
    else:
        logging.info(f"JSON not found, falling back to text format: {txt_path}")
        vulnerabilities = parse_filtered_vulnerabilities(txt_path)

        if not vulnerabilities:
            logging.info("No vulnerabilities to process.")
            return

        create_issues(vulnerabilities)

if __name__ == "__main__":
    main()
