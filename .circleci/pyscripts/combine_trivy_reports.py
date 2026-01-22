import re
import os
import logging
import glob

vulnerabilities = []

def parse_filtered_vulnerabilities(file_path):
    global vulnerabilities

    # Get * in *-filtered_vulnerabilities.txt
    pattern=re.compile(r'(.*)-image-single-arch-linux-amd64-trivy_filtered_vulnerabilities\.txt')
    print(f"Parsing filtered vulnerabilities from {file_path}")
    print("")
    match=pattern.match(file_path.split('/')[-1])
    if match:
        source=match.group(1)
        print(f"Source identified as: {source}")
    else:
        print("Could not identify source from filename.")
        return []
        
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
                payload={
                    'VulnerabilityID': fields[0].strip(),
                    'Severity': fields[1].strip(),
                    'Status': fields[2].strip(),
                    'InstalledVersion': fields[3].strip(),
                    'FixedVersion': fields[4].strip(),
                    'Class': fields[5].strip(),
                    'Target': fields[6].strip(),
                    'PkgName': fields[7].strip(),
                    'PkgPath': fields[8].strip(),
                    'Title': fields[9].strip(),
                    'Products': source
                }
                print(f"Parsed vulnerability: {payload['VulnerabilityID']} from source: {source}")
                # Check for duplicates before adding, Products could have different values,
                # we should add the source to Products if the vulnerability already exists
                if not vulnerabilities:
                    vulnerabilities.append(payload)
                else:
                    for vuln in vulnerabilities:
                        if vuln['VulnerabilityID'] == payload['VulnerabilityID']:
                            if source not in vuln['Products'].split(', '):
                                vuln['Products'] += f", {source}"
                            break
                        else:
                            vulnerabilities.append(payload)
                            break

    return vulnerabilities


if __name__ == "__main__":
    for json_file in glob.glob('/home/circleci/project/artifacts/*_filtered_vulnerabilities.json'):
        
        print(f"Processing JSON file: {json_file}")
        # Here you would call analyze_trivy_report.py logic to generate filtered_vulnerabilities.txt
        # For this example, we assume that the filtered files are already generated
        import subprocess
        output=subprocess.run([
            "python3", "/home/circleci/project/.circleci/pyscripts/analyze_trivy_report.py", json_file
        ], capture_output=True, text=True)
        print("Subprocess output:")
        print(output.stdout)
        if output.stderr:
            print("Subprocess errors (if any):")
            print(output.stderr)
        print(f"Completed analysis for {json_file}")

    with open('/home/circleci/project/artifacts/filtered_vulnerabilities.txt', 'a') as outfile:
        outfile.write("VulnerabilityID | Severity | Status | InstalledVersion | FixedVersion | Class | Target | PkgName | PkgPath | Title | Products\n")
        outfile.write("-" * 150 + "\n")

    for file_path in glob.glob('/home/circleci/project/*-image-single-arch-linux-amd64-trivy_filtered_vulnerabilities.txt'):
        vulnerabilities = parse_filtered_vulnerabilities(file_path)
        print(f"Parsed {len(vulnerabilities)} vulnerabilities from {file_path}")
        with open('/home/circleci/project/artifacts/filtered_vulnerabilities.txt', 'a') as outfile:
            for vuln in vulnerabilities:
                print(f"Writing vulnerability {vuln['VulnerabilityID']} to output file.")
                outfile.write(f"{vuln['VulnerabilityID']} | {vuln['Severity']} | {vuln['Status']} | {vuln['InstalledVersion']} | {vuln['FixedVersion']} | {vuln['Class']} | {vuln['Target']} | {vuln['PkgName']} | {vuln['PkgPath']} | {vuln['Title']} | {vuln['Products']}\n")
