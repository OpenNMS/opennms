import re
import logging
import glob

vulnerabilities = []

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

def parse_filtered_vulnerabilities(file_path):
    global vulnerabilities

    # Get * in *-filtered_vulnerabilities.txt
    pattern=re.compile(r'(.*)-image-single-arch-linux-amd64-trivy_filtered_vulnerabilities\.txt')
    logging.info(f"Parsing filtered vulnerabilities from {file_path}")
    logging.info("")
    match=pattern.match(file_path.split('/')[-1])
    if match:
        source=match.group(1)
        logging.info(f"Source identified as: {source}")
    else:
        logging.error("Could not identify source from filename.")
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
                # logging.info(f"Parsed vulnerability: {payload['VulnerabilityID']} from source: {source}")
                # Check for duplicates before adding, Products could have different values,
                # we should add the source to Products if the vulnerability already exists
                # NOTE: Deduplication must consider VulnerabilityID + PkgName + PkgPath because
                # the same CVE can affect different packages or different versions of the same package
                existing_vuln = next(
                    (v for v in vulnerabilities if v['VulnerabilityID'] == payload['VulnerabilityID']
                     and v['PkgName'] == payload['PkgName']
                     and v['PkgPath'] == payload['PkgPath']),
                    None
                )
                if existing_vuln:
                    # existing_vuln['Products'] could be a string or a list, we need to handle both cases
                    # convert it to list if it's not already a list, then append the new source if it's not already in the list
                    if source not in existing_vuln['Products']:
                        # if it's a string with commas, split it first
                        if ", " in existing_vuln['Products'] or "," in existing_vuln['Products']:
                            existing_vuln['Products'] = existing_vuln['Products'].split(", ")
                        elif isinstance(existing_vuln['Products'], str):
                            existing_vuln['Products'] = [existing_vuln['Products']]

                        # Now append the new source
                        existing_vuln['Products'].append(source)
                else:
                    vulnerabilities.append(payload)

    return vulnerabilities


if __name__ == "__main__":

    # Determine paths based on environment (CircleCI vs local)
    circleci_base = '/home/circleci/project'
    
    base_path = circleci_base
    json_path = f'{base_path}/artifacts/vulnerabilities_by_package.json'
    txt_path = f'{base_path}/artifacts/filtered_vulnerabilities.txt'
    blocklist_path = f'{base_path}/.circleci/trivy-config/blocked_list.json'

    # Check if *-image-single-arch-linux-amd64-trivy_filtered_vulnerabilities.txt files exist
    if not glob.glob(f'{base_path}/*-image-single-arch-linux-amd64-trivy_filtered_vulnerabilities.txt'):
        logging.error("No *-image-single-arch-linux-amd64-trivy_filtered_vulnerabilities.txt files found. Exiting.")
        exit(1)

    with open(f'{base_path}/artifacts/filtered_vulnerabilities.txt', 'a') as outfile:
        outfile.write("VulnerabilityID | Severity | Status | InstalledVersion | FixedVersion | Class | Target | PkgName | PkgPath | Title | Products\n")
        outfile.write("-" * 150 + "\n")

    for file_path in glob.glob(f'{base_path}/*-image-single-arch-linux-amd64-trivy_filtered_vulnerabilities.txt'):
        parse_filtered_vulnerabilities(file_path)
        logging.info(f"Parsed {len(vulnerabilities)} total vulnerabilities so far")

    # Write all deduplicated vulnerabilities once at the end
    with open(f'{base_path}/artifacts/filtered_vulnerabilities.txt', 'a') as outfile:
        for vuln in vulnerabilities:
            # logging.info(f"Writing vulnerability {vuln['VulnerabilityID']} to output file.")
            outfile.write(f"{vuln['VulnerabilityID']} | {vuln['Severity']} | {vuln['Status']} | {vuln['InstalledVersion']} | {vuln['FixedVersion']} | {vuln['Class']} | {vuln['Target']} | {vuln['PkgName']} | {vuln['PkgPath']} | {vuln['Title']} | {', '.join(vuln['Products'])}\n")
