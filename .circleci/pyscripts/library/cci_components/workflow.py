from library import libfile
from library import common
import re 

class workflow:
    _internal_workflow={}

    def __init__(self)->None:
        self._file_library=libfile.libfile()
        self._common_library=common.common()
        
        pass
    
    def load(self,path)->bool:
        self._internal_workflow=self._file_library.load_json(path)

        if self._internal_workflow:
            return True
        else:
            return False



    def generate_dependency(self,workflow):
        #print("generate_dependency:",workflow)
        outputList=[workflow]
        
        for a in self._internal_workflow:
            for b in self._internal_workflow[a]:
                if re.match("^"+workflow+"$",b):
                    if a == "bundles":
                        # When looking at bundles, we don't want to 
                        # add the bundle name into the dependency list
                        if outputList[-1] == b:
                            outputList.pop(-1)

                    #print("Found",workflow,"in",a)
                    for c in self._internal_workflow[a][b]:
                        if "requires" in c:
                            for d in self._internal_workflow[a][b][c]:
                                outputList.append(d)
                        elif "extends" in c:
                            for d in self._internal_workflow[a][b][c]:
                                _output=self.generate_dependency(d)
                                if type(_output) == list:
                                    outputList.extend(_output)
                                else:
                                    outputList.append(_output)
        return outputList
        
    def find(self,workflow):
        for a in self._internal_workflow:
            for b in self._internal_workflow[a]:
                if b == workflow:
                    return self._internal_workflow[a][b]


    def get_workflow_yaml(self,workflow,leadingSpace=0,enable_filters=True):
        workflow_dependency=self.get_dependency(workflow)
        #print("get_worflow_yaml::Dependencies>",workflow_dependency)
        #print("get_worflow_yaml::leadingSpace>",leadingSpace)
        
        tmp_output=[]

        for i in workflow_dependency:
            # do we have any items under this key
            tmp_output_elements=self.find(i)

            if "job" in tmp_output_elements:
                tmp_output.append(self._common_library.create_space(leadingSpace)+"- "+tmp_output_elements["job"])
                del tmp_output_elements["job"]
            else:
                tmp_output.append(self._common_library.create_space(leadingSpace)+"- "+i)

            if "extends" in tmp_output_elements:
                # Since we have expanded the dependency we don't need this anymore
                del tmp_output_elements["extends"]
            
            if "filters" in tmp_output_elements and not enable_filters:
                #If we have disabled filters
                print("Deleting workflow filters as the user has disabled them")
                del tmp_output_elements["filters"]
                

            # if we have any items, lets add the : after the entry
            if tmp_output_elements:
                tmp_output[-1]+=":"

            # lets loop through the elements
            for e in tmp_output_elements:
                if "filters" in e:
                    tmp_output.append(self._common_library.create_space(leadingSpace+4)+"filters:")
                    for f in tmp_output_elements[e]:
                        if type(tmp_output_elements[e][f]) == dict:
                            tmp_output.append(self._common_library.create_space(leadingSpace+6)+f+":")
                            for fe in tmp_output_elements[e][f]:
                                tmp_output.append(self._common_library.create_space(leadingSpace+8)+fe+":")
                                for ff in tmp_output_elements[e][f][fe]:
                                    tmp_output.append(self._common_library.create_space(leadingSpace+10)+"- "+ff+"")
                
                elif "variations" in e:
                    tmp_output.append(self._common_library.create_space(leadingSpace+4)+"matrix:")
                    tmp_output.append(self._common_library.create_space(leadingSpace+6)+"parameters:")
                    tmp_output.append(self._common_library.create_space(leadingSpace+8)+"architecture:"+str(tmp_output_elements[e]))
                
                elif "context" in e:
                    tmp_output.append(self._common_library.create_space(leadingSpace+4)+"context:")
                    for f in tmp_output_elements[e]:
                        tmp_output.append(self._common_library.create_space(leadingSpace+6)+"- "+f)

                elif "requires" in e:
                    tmp_output.append(self._common_library.create_space(leadingSpace+4)+"requires:")  
                    for f in tmp_output_elements[e]:
                        tmp_output.append(self._common_library.create_space(leadingSpace+6)+"- "+f)
                        if f not in workflow_dependency:
                            tmp_output.extend(self.get_workflow_yaml(f,leadingSpace,enable_filters))

                else:
                    print("Problem!!! Not sure how to handle element ",e)

        return tmp_output

    def get_dependency(self,workflow):
        #print("get_dependency:",workflow)

        tmp_output=self.generate_dependency(workflow)
        #print("Pre-Clean Up:",tmp_output,len(tmp_output))

        tmp_output2=list(set(tmp_output))
        #print("Post-Clean Up:",tmp_output2,len(tmp_output2))

        return tmp_output2