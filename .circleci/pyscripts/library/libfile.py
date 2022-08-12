import glob
import os
import shutil
import json


class libfile:
    def getLatestFile(self, path):
        list_of_files = glob.glob(os.path.join(path, "*"))
        return max(list_of_files, key=os.path.getctime)

    def read_file(self, path):
        with open(path, "r", encoding="UTF-8") as file_handler:
            return file_handler.readlines()

    def write_file(self, path, content):
        with open(path, "w", encoding="UTF-8") as file_handler:
            file_handler.write(content)

    def removefolder(self, path):
        return shutil.rmtree(path)

    def load_json(self, path):
        with open(path, "r", encoding="UTF-8") as file_handler:
            return json.load(file_handler)

    def save_json(self, path, content):
        with open(path, "w", encoding="UTF-8") as file_handler:
            json.dump(content, file_handler, indent=4)

    def find_files(self, start_path, pattern, output=[]):
        current_files = os.listdir(start_path)
        for entry in current_files:
            tmp_path = os.path.join(start_path, entry)
            if os.path.isdir(tmp_path):
                self.find_files(tmp_path, pattern, output)
            elif pattern in entry:
                output.append(tmp_path)
        return output
