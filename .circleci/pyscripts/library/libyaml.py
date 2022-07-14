import re,glob

class libyaml:
    def __init__(self) -> None:
        self._finalList={}
        self.job_list={}
        self.job_positions=[]

        self.step_regx="^(?!#)\s+\-.*:" #match all lines but ignore the ones starting with #

    def read_file(self,input):
        with open(input,"r") as f:
            content=f.readlines()
        return content

    def find_dep(self,key,job_list):
        _requires=[]
        if key not in job_list:
            return 
        reqfound=False
        filtersFound=False
        OtherFinds=False
        for e in job_list[key]["raw"]:
            if "requires" in e:
                reqfound=True 
                OtherFinds=False
                filtersFound=False
                continue #jump to next line
            if "filters" in e:
                reqfound=False
                filtersFound=True
                OtherFinds=False
                continue #jump to next line
            if e.strip().replace("- ","") in ["matrix:","parameters","architecture"]:
                reqfound=False
                filtersFound=False
                OtherFinds=True
                continue
            if reqfound and not filtersFound and not OtherFinds:
                if "#" not in e:
                    _requires.append(e.strip().replace("- ",""))
        return _requires

    def setup(self,workflow_path):
        files=glob.glob(workflow_path)
        for file in files:
            job_list={}
            job_positions=[]
            _content=self.read_file(file)
            for line in _content:
                if re.match(self.step_regx,line):
                    if "equal" not in line:
                        job_list[line.strip().replace("- ","").replace(":","")]={}
                        job_positions.append(_content.index(line) )         

            for e,k in enumerate(job_positions):
                if len(job_positions) != e+1:
                    if _content[k].strip().replace("- ","").replace(":","") in job_list:
                        job_list[_content[k].strip().replace("- ","").replace(":","")]["raw"]=_content[k+1:job_positions[e+1]]
                else:
                    #If we are the last entry; lets assume we can go until end of file
                    job_list[_content[k].strip().replace("- ","").replace(":","")]["raw"]=_content[k+1:]

            for k in job_list:
                _requires=self.find_dep(k,job_list)
                job_list[k]["requires"]=_requires
                if k not in self._finalList:
                    self._finalList[k]={}

                self._finalList[k]["requires"]=_requires
        
    def tell_requirements(self,component):
        if not self._finalList or ("requires" not in self._finalList[component]):
            return ""
        _output1=[]
        if self._finalList[component]['requires']:
            for _d in self._finalList[component]['requires']:
                _output1.append(_d)
        return _output1

            
    def tell_extended_requirements(self,component):
        if not self._finalList or  "requires" not in self._finalList[component]:
            return
        _output1=[]
        if not self._finalList[component]['requires']:
            return []
        for _d in self._finalList[component]['requires']:
            _output1.append(_d)
            _extended=self.tell_extended_requirements(_d)
            if _extended:
                _output1.append(_extended)
        return _output1

    def dependency_to_str(self,input,_start=""):
        for e in input:
            if type(e) == list:
                _start+=self.dependency_to_str(e)
            else:
                _start+=e+"->"
        return _start

    def print_list(self,input):
        for e in input:
            print("\t","*",e)

    def detect_dependencies(self,input):
        _output={}
        _output[input]={}
        _output[input]["requires"]=[]
        
        _text=self.dependency_to_str(self.tell_extended_requirements(input),"")
        _res=[]
        for e in _text.split("->"):
            if e not in _res and e.strip():
                if e not in _output[input]["requires"]:
                    _output[input]["requires"].append(e)

        return _output


    def create_space(self,level=0):
        tmp_line=" "*level
        return tmp_line

    def generate_yaml(self,input_json,key,level=0,_line=[],disable_filters=False):
        for entry in input_json[key]:
            if entry in ["extends"]:
                for extension in input_json[key][entry]:
                    if not [i for i in _line if "- "+extension in i]: 
                        self.generate_yaml(input_json,extension,level,_line)
            else:
                if not [i for i in _line if "- "+entry in i]: 
                    if input_json[key][entry]:
                        _line.append(self.create_space(level)+"- "+entry+":")
                        for entry_lvl2 in input_json[key][entry]:
                            if entry_lvl2 in "variations":
                                tmp_data=self.create_space(level+4)+"matrix:\n"
                                tmp_data+=self.create_space(level+6)+"parameters:\n"
                                tmp_data+=self.create_space(level+8)+"architecture:"+str(input_json[key][entry][entry_lvl2])
                                _line.append(tmp_data)
                            elif entry_lvl2 in "context":
                                _line.append(self.create_space(level+4)+"context:")
                                for entry_lvl3 in input_json[key][entry][entry_lvl2]:
                                    _line.append(self.create_space(level+6)+"- "+entry_lvl3)
                            elif entry_lvl2 in "requires":
                                _line.append(self.create_space(level+4)+"requires:")
                                for entry_lvl3 in input_json[key][entry][entry_lvl2]:
                                    _line.append(self.create_space(level+6)+"- "+entry_lvl3)
                            elif entry_lvl2 in "filters":
                                if not disable_filters:
                                    _line.append(self.create_space(level+4)+"filters:")
                                    for entry_lvl3 in input_json[key][entry][entry_lvl2]:
                                        if type(input_json[key][entry][entry_lvl2][entry_lvl3]) == dict:
                                            _line.append(self.create_space(level+6)+""+entry_lvl3+":")
                                            for entry_lvl4 in input_json[key][entry][entry_lvl2][entry_lvl3]:
                                                _line.append(self.create_space(level+8)+""+entry_lvl4+":")
                                                for entry_lvl5 in input_json[key][entry][entry_lvl2][entry_lvl3][entry_lvl4]:
                                                    _line.append(self.create_space(level+10)+"- "+entry_lvl5)
                            else:
                                _line.append(self.create_space(level+2)+"- "+str(input_json[key][entry][entry_lvl2]))
                    else:
                        _line.append(self.create_space(level)+"- "+entry)

        return _line