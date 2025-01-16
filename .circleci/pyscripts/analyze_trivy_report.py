import json
import argparse
import os

# Set up command-line argument parsing
parser = argparse.ArgumentParser(description='Process Trivy JSON report.')
parser.add_argument('json_file', type=str, help='Path to the JSON report file (e.g., /tmp/report.json)')
args = parser.parse_args()

# Load the Trivy output
with open(args.json_file) as f:
    data = json.load(f)

# List to store filtered vulnerabilities
filtered_vulnerabilities = []

# Mapping of classes
class_mapping = {
    'os': 'OS',
    'os-pkgs': 'OS',
    'library': 'Library',
    'application code': 'Application Code',
    'lang-pkgs': 'Lang Packages',
    'container': 'Container',
    'file': 'File',
    'git': 'Git',
    'kubernetes': 'Kubernetes'
}

# Extract relevant information for "Critical", "High", "Medium", and "Low" severities
for result in data.get('Results', []):
    for vulnerability in result.get('Vulnerabilities', []):
        severity = vulnerability.get('Severity')
        if severity in ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']:
            vulnerability_id = vulnerability.get('VulnerabilityID')
            vulnerability_class = result.get('Class')
            category = class_mapping.get(vulnerability_class)
            target = result.get('Target')
            pkg_name = vulnerability.get('PkgName', 'N/A')
            pkg_path = vulnerability.get('PkgPath', 'N/A')
            
            # Append relevant information to the list
            if vulnerability_id and category:
                filtered_vulnerabilities.append({
                    'VulnerabilityID': vulnerability_id,
                    'Severity': severity,
                    'Status': vulnerability.get('Status', 'N/A'),
                    'InstalledVersion': vulnerability.get('InstalledVersion', 'N/A'),
                    'FixedVersion': vulnerability.get('FixedVersion', 'N/A'),
                    'Class': category,
                    'Target': target,
                    'PkgName': pkg_name,
                    'PkgPath': pkg_path,
                    'Title': vulnerability.get('Title', 'N/A')
                })

# Get the base name for the output files
base_name = os.path.splitext(os.path.basename(args.json_file))[0]

# Save to a text file
with open(f'{base_name}.txt', 'w') as outfile:
    header = " VulnerabilityID     | Severity | Status               | Installed Version                   | Fixed Version                        | Class         | Target                              | PkgName                                     | PkgPath                             | Title"
    outfile.write(header + '\n')
    outfile.write("-" * len(header) + '\n')
    for v in filtered_vulnerabilities:
        outfile.write(f"{v['VulnerabilityID']: <20} | {v['Severity']: <8} | {v['Status']: <20} | {v['InstalledVersion']: <35} | {v['FixedVersion']: <36} | {v['Class']: <13} | {v['Target']: <35} | {v['PkgName']: <43} | {v['PkgPath']: <109} | {v['Title']}\n")

# Save to a CSV file
with open(f'{base_name}.csv', 'w') as outfile:
    header = [
        'VulnerabilityID',
        'Severity',
        'Status',
        'InstalledVersion',
        'FixedVersion',
        'Class',
        'Target',
        'PkgName',
        'PkgPath',
        'Title'
    ]
    outfile.write(','.join(header) + '\n')  # Write header
    for v in filtered_vulnerabilities:
        # Use a simple CSV format
        title = v["Title"].replace('"', '""')  # Escape double quotes in title
        fixed_version = v["FixedVersion"].replace('"', '""')  # Escape double quotes in FixedVersion

        line = [
            v['VulnerabilityID'],
            v['Severity'],
            v['Status'],
            v['InstalledVersion'],
            f'"{fixed_version}"',  # Enclose FixedVersion in quotes
            v['Class'],
            v['Target'],
            v['PkgName'],
            v['PkgPath'],
            f'"{title}"'  # Enclose title in quotes
        ]
        outfile.write(','.join(line) + '\n')

print(f"Output saved to '{base_name}.txt' and '{base_name}.csv'")