import re
import os 
import json
import shutil

class common:
    def read_file(self,path):
        _tmp=""
        with open(path,"r") as f:
            _tmp=f.readlines()
        return _tmp
    
    def load_json(self,path):
        _tmp={}
        with open(path,"r") as f:
            _tmp=json.load(f)
        return _tmp

    def save_json(self,path,content):
        with open(path,"r") as f:
            json.dump(content,f)

    def extract_keywords(self,path):
        re_pattern=re.compile("^.*#.*#")
        keywords={}
        for line in self.read_file(path):
            re_match=re.match(re_pattern,line)
            if re_match:
                block=re_match.group().split(":")[0].strip().replace("#","")
                if block not in keywords:
                    keywords[block]={}
                keywords[block][re_match.group().strip()]={
                    "block_indentation":len(re.findall(' ',line))
                }
        return keywords
    
    #This is incorrect, we are always adding to the same tmp_output 
    def expand_index(self,index,path_to_main_folder,tmp_output=[]):
        folder=index.replace("#","").split(":")[0].strip()
        filepath=index.replace("#","").split(":")[1].strip()
        file_content=self.read_file(os.path.join(path_to_main_folder,folder,filepath))
        re_pattern=re.compile("^.*#.*#")
        for entry in file_content:
            tmp_match=re.match(re_pattern,entry)
            if tmp_match:
                if ".index" in tmp_match.group().strip():
                    print("We do not support nested index files")
                else:
                    tmp_output.append(self.expand_keyword(tmp_match.group().strip(),path_to_main_folder))
        
        shutil.move(os.path.join(path_to_main_folder,folder,filepath),os.path.join(path_to_main_folder,folder,filepath+"_DONE"))

                
        return tmp_output
    
    def expand_keyword(self,index,path_to_main_folder):
        folder=index.replace("#","").split(":")[0].strip()
        filepath=index.replace("#","").split(":")[1].strip()+".yml"
        file_content=self.read_file(os.path.join(path_to_main_folder,folder,filepath))[1:]

        shutil.move(os.path.join(path_to_main_folder,folder,filepath),os.path.join(path_to_main_folder,folder,filepath+"_DONE"))

        return file_content

    def find_files(self,start_path,pattern,output=[]):
        current_files=os.listdir(start_path)
        for entry in current_files:
            tmp_path=os.path.join(start_path,entry)
            if os.path.isdir(tmp_path):
                self.find_files(tmp_path,pattern,output)
            elif pattern in entry:
                output.append(tmp_path)
        return output