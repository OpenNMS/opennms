import re,glob
from library import libfile

class libyaml_v2:
    def __init__(self) -> None:
        self._finalList={}
        self.job_list={}
        self.job_positions=[]
        self.libfile=libfile.libfile()
        self.requirementsList=[]
        self.processedList=[]

        self.step_regx="^(?!#)\s+\-.*:" #match all lines but ignore the ones starting with #

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

    def json_setup(self,json_path):
        self._finalList=self.libfile.load_json(json_path)
        self._finalList["format"]="json"

    def setup(self,workflow_path):
        if ".json" in workflow_path:
            self.json_setup(workflow_path)
            return
        self._finalList["format"]="yaml"

        files=glob.glob(workflow_path)
        for file in files:
            job_list={}
            job_positions=[]
            _content=self.libfile.read_file(file)
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

            
    def tell_extended_requirements_from_json(self,component,requirementsList=[],processedList=[]):
        for key in self._finalList:
            if key in "format":
                continue
            for key_lvl2 in self._finalList[key]:
                if re.match("^"+component+"$",key_lvl2):
                    if "requires" in self._finalList[key][key_lvl2]:
                        for require in self._finalList[key][key_lvl2]["requires"]:
                            if require not in self.requirementsList:
                                self.requirementsList.append(require)
                                self.tell_extended_requirements_from_json(require,self.requirementsList,self.processedList)

                    if "extends" in self._finalList[key][key_lvl2]:
                        for require in self._finalList[key][key_lvl2]["extends"]:
                            if require not in self.requirementsList:
                                self.requirementsList.append(require)

                            self.tell_extended_requirements_from_json(require,self.requirementsList,self.processedList)
                    self.processedList.append(key_lvl2)
        return self.requirementsList


    def tell_extended_requirements(self,component,processedList=[]):
        if self._finalList["format"] == "json":
            return self.tell_extended_requirements_from_json(component,self.processedList)
            
        if not self._finalList or  "requires" not in self._finalList[component]:
            return ""
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


    def generate_workflows_single(self,input_json,key,level=0,output=[],enable_filters=False,processedList=[]):
        bundles=key in list(input_json["bundles"].keys())
        individual=key in list(input_json["individual"].keys())

        if bundles:
            subkey="bundles"
        elif individual:
            subkey="individual"

        working_data=input_json[subkey][key]

        if "job" in working_data:
            job_name=working_data["job"]
            del working_data["job"]
        else:
            job_name=key
        
        if not enable_filters:
            if "filters" in working_data:
                del working_data["filters"]

        #if not [i for i in output if self.create_space(level-2)+"- "+key in i] :
        if not [i for i in output if re.match("^"+self.create_space(level)+"- "+job_name,i)] :
            if working_data:
                if len(working_data) == 1 and "extends" in working_data:
                    output.append(self.create_space(level)+"- "+job_name+"")
                else:
                    output.append(self.create_space(level)+"- "+job_name+":")

                for item in working_data:
                    if "requires" in item:
                        output.append(self.create_space(level+4)+"requires:")  
                        for entry_lvl3 in working_data[item]:
                            output.append(self.create_space(level+6)+"- "+entry_lvl3)
                    elif "filters" in item and enable_filters:
                        output.append(self.create_space(level+4)+"filters:")  
                        for entry_lvl3 in working_data[item]:
                            if type(working_data[item][entry_lvl3]) == dict:
                                output.append(self.create_space(level+6)+""+entry_lvl3+":")
                                for entry_lvl4 in working_data[item][entry_lvl3]:
                                    output.append(self.create_space(level+8)+""+entry_lvl4+":")
                                    for entry_lvl5 in working_data[item][entry_lvl3][entry_lvl4]:
                                        output.append(self.create_space(level+10)+"- "+entry_lvl5)
                    elif "variations" in item:
                                tmp_data=self.create_space(level+4)+"matrix:\n"
                                tmp_data+=self.create_space(level+6)+"parameters:\n"
                                tmp_data+=self.create_space(level+8)+"architecture:"+str(working_data[item])
                                output.append(tmp_data)
                    elif "context" in item:
                                output.append(self.create_space(level+4)+"context:")
                                for entry_lvl3 in working_data[item]:
                                    output.append(self.create_space(level+6)+"- "+entry_lvl3)
                    elif "extends" in item:
                        for entry_lvl2 in input_json[subkey][key][item]:
                            self.generate_workflows(input_json,entry_lvl2,level,output,enable_filters,self.processedList)
                    else:
                        print("Found a item that is not yet implemented in generate_workflows_single function:",item)

            else:
                output.append(self.create_space(level)+"- "+job_name)

        return output
            
    # Use tell_extended_requirements to figure out what we need to do :)
    def generate_workflows(self,input_json,key,level=0,output=[],enable_filters=False,processedList=[]):
        build_dependencies=self.tell_extended_requirements(key,self.processedList)
        self.requirementsList=build_dependencies
        if len(build_dependencies) > 1:
            for dependency in build_dependencies:
                if dependency not in output:
                    if dependency not in processedList:
                        output= self.generate_workflows_single(input_json,dependency,level,output,enable_filters,self.processedList)
                        self.processedList.append(dependency)
                    else:
                        continue

                output= self.generate_workflows_single(input_json,dependency,level,output,enable_filters,self.processedList)
        else:
            output=self.generate_workflows_single(input_json,key,level,output,enable_filters,self.processedList)

            if len(self.requirementsList)>0:
                for req in self.requirementsList:
                    output=self.generate_workflows_single(input_json,req,level,output,enable_filters,self.processedList)
                    self.processedList.append(req)
            else:
                self.processedList.append(key)
        
        return output

    def clean(self):
        self.requirementsList=[]
        self.processedList=[]
