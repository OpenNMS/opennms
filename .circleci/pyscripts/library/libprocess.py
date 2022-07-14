#A0
import subprocess
import datetime
import os

class libprocess:
    def saveFile(self,filename,data):
        if not data:
            return
        if os.path.exists(filename):
            with open(filename,"a") as f:
                f.write("\n_____________\n")

        with open(filename,"a") as f:
            if type(data) == list:
                f.writelines("\n".join(data))
            elif type(data) == subprocess.CompletedProcess:
                f.write("Return code: "+str(data.returncode)+"\n")
                f.write("===STDERR\n")
                f.write(str(data.stderr.decode("utf-8"))+"\n")
                f.write("===STDOUT\n")
                f.write(str(data.stdout.decode("utf-8"))+"\n")
            else:
                f.write(data)
                
    def runProcess(self,command,working_directory="",environment="",redirectSTDOUT=False,redirectSTDERR=False,outputFile=""):
        print("Command:"," ".join(command))
        if redirectSTDOUT:
            _redirectSTDOUT=subprocess.PIPE
        else:
            _redirectSTDOUT=None

        if redirectSTDERR:
            _redirectSTDERR=subprocess.PIPE
        else:
            _redirectSTDERR=None

        _start=datetime.datetime.now()
        if environment:
            _output=subprocess.run(command,env=environment,cwd=working_directory,stdout=_redirectSTDOUT,stderr=_redirectSTDERR)
        else:
            _output=subprocess.run(command,cwd=working_directory,stdout=_redirectSTDOUT,stderr=_redirectSTDERR)
        _end=datetime.datetime.now()

        if outputFile == "stdout":
            return_data={
            "Time Started":_start.strftime("%Y/%m/%d %H:%M:%S.%f"),
            "Time Finnished":_end.strftime("%Y/%m/%d %H:%M:%S.%f"),
            "Time Taken":str(_end-_start),
            "Output":{
                "stdout":str(_output.stdout.decode("utf-8"))+"\n",
                "stderr":str(_output.stderr.decode("utf-8"))+"\n",
            },
            "Return Code":_output.returncode
            }
        else:
            self.saveFile(outputFile,_output)
            return_data={
            "Time Started":_start.strftime("%Y/%m/%d %H:%M:%S.%f"),
            "Time Finnished":_end.strftime("%Y/%m/%d %H:%M:%S.%f"),
            "Time Taken":str(_end-_start),
            "Output":outputFile,
            "Return Code":_output.returncode
        }

        return return_data

    def printSummary(self,result):
        print("Time Taken:",result["Time Started"])
        print("Time Finnished:",result["Time Finnished"])
        print("Time Taken:",result["Time Finnished"])
        print("Return Code:",result["Return Code"])

        if "stdout" in result["Output"] and "stderr" in result["Output"]:
            return 

        if os.path.exists(result["Output"]):
            with open(result["Output"],"r") as f:
                _output=f.readlines()
        
        stdoutPosition=_output.index("===STDOUT\n")
        stderrPosition=_output.index("===STDERR\n")

        stderr_lines=list(filter(None,[ l.strip() for l in _output[stderrPosition+1:stdoutPosition]]))
        stdout_lines=list(filter(None,[ l.strip() for l in _output[stdoutPosition+1:]]))

        print(">>","Last",3,"lines:")
        if stdout_lines:
            print(">","STDOUT>")
            for l in stdout_lines[-3:]:
                print(" "," ",l)
        if stderr_lines:
            print(">","STDERR>")
            for l in stderr_lines[-3:]:
                print(" "," ",l)