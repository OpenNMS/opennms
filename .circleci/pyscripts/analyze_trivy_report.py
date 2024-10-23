import json

# Load the Trivy output
with open('/tmp/report.json') as f:
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
                    'Class': category,
                    'VulnerabilityID': vulnerability_id,
                    'Target': target,
                    'PkgName': pkg_name,
                    'PkgPath': pkg_path,
                    'Severity': severity,
                    'Status': vulnerability.get('Status', 'N/A'),
                    'InstalledVersion': vulnerability.get('InstalledVersion', 'N/A'),
                    'FixedVersion': vulnerability.get('FixedVersion', 'N/A'),
                    'Title': vulnerability.get('Title', 'N/A')
                })

# Save to a text file
with open('filtered_vulnerabilities.txt', 'w') as outfile:
    header = "Class       | VulnerabilityID       | Target                               | PkgName                       | PkgPath                             | Severity | Status | Installed Version | Fixed Version       | Title"
    outfile.write(header + '\n')
    outfile.write("-" * len(header) + '\n')
    for v in filtered_vulnerabilities:
        outfile.write(f"{v['Class']: <15} | {v['VulnerabilityID']: <20} | {v['Target']: <35} | {v['PkgName']: <30} | {v['PkgPath']: <35} | {v['Severity']: <8} | {v['Status']: <6} | {v['InstalledVersion']: <17} | {v['FixedVersion']: <18} | {v['Title']}\n")

# Save to a CSV file
with open('filtered_vulnerabilities.csv', 'w') as outfile:
    header = [
        'Class',
        'VulnerabilityID',
        'Target',
        'PkgName',
        'PkgPath',
        'Severity',
        'Status',
        'InstalledVersion',
        'FixedVersion',
        'Title'
    ]
    outfile.write(','.join(header) + '\n')  # Write header
    for v in filtered_vulnerabilities:
        # Use a simple CSV format
        title = v["Title"].replace('"', '""')
        fixed_version = v["FixedVersion"].replace('"', '""')

        line = [
            v['Class'],
            v['VulnerabilityID'],
            v['Target'],
            v['PkgName'],
            v['PkgPath'],
            v['Severity'],
            v['Status'],
            v['InstalledVersion'],
            f'"{fixed_version}"',
            f'"{title}"'
        ]
        outfile.write(','.join(line) + '\n')

print("Output saved to 'filtered_vulnerabilities.txt' and 'filtered_vulnerabilities.csv'")
