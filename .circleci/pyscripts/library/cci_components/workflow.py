import os
import re
import json

from library import common


class workflow:
    """
    Represent a workflow in CircleCI object, and contains supporting
    functionality to help with generating Yaml entry
    """

    _internal_workflow = {}
    _analyzed_dependencies = []

    def __init__(self) -> None:
        self._common_library = common.common()

    def load(self, path) -> bool:
        """
        This function is used to load workflow json file
        """
        with open(path, "r", encoding="UTF-8") as file_handler:
            self._internal_workflow = json.load(file_handler)

        return bool(self._internal_workflow)

    def generate_dependency(self, current_workflow):
        """
        Returns the build dependency for a workflow
        """
        output_list = [current_workflow]

        for workflow_type in self._internal_workflow:
            for workflow_key in self._internal_workflow[workflow_type]:
                if re.match("^" + current_workflow + "$", workflow_key):
                    if workflow_type == "bundles":
                        # When looking at bundles, we don't want to
                        # add the bundle name into the dependency list
                        if output_list[-1] == workflow_key:
                            output_list.pop(-1)

                    for workflow_property in self._internal_workflow[workflow_type][
                        workflow_key
                    ]:
                        if "requires" in workflow_property:
                            for entry in self._internal_workflow[workflow_type][
                                workflow_key
                            ][workflow_property]:
                                output_list.append(entry)
                        elif "extends" in workflow_property:
                            for extended_workflow in self._internal_workflow[
                                workflow_type
                            ][workflow_key][workflow_property]:
                                _output = self.generate_dependency(extended_workflow)
                                if isinstance(_output, list):
                                    output_list.extend(_output)
                                else:
                                    output_list.append(_output)
        return output_list

    def find(self, interested_workflow) -> dict:
        """
        Find a workflow in our internal workflow list
        """
        for workflow_type in self._internal_workflow:
            for workflow_name in self._internal_workflow[workflow_type]:
                if workflow_name == interested_workflow:
                    return self._internal_workflow[workflow_type][workflow_name]

    def get_workflow_yaml(
        self, interested_workflow, leading_space=0, enable_filters=True
    ) -> list:
        """
        Returns a list containing yaml entries for the workflow
        """

        workflow_dependency = self.get_dependency(interested_workflow)

        tmp_output = []

        workflow_jobs = set()
        # Let's find all the jobs we depend on (or need)
        for dependency in workflow_dependency:
            print("\t", "Processing", dependency)
            tmp_output_elements = self.find(dependency)
            print("\t", "\t", tmp_output_elements)
            workflow_jobs.add(dependency)
            if "requires" in tmp_output_elements:
                for require in tmp_output_elements["requires"]:
                    workflow_jobs.add(require)

        for job in sorted(workflow_jobs):
            tmp_output_elements = self.find(job)

            if "job" in tmp_output_elements:
                tmp_output.append(
                    self._common_library.create_space(leading_space)
                    + "- "
                    + tmp_output_elements["job"]
                )
                del tmp_output_elements["job"]
            else:
                tmp_output.append(
                    self._common_library.create_space(leading_space) + "- " + job
                )
            if "extends" in tmp_output_elements:
                # Since we have expanded the dependency we don't need this anymore
                del tmp_output_elements["extends"]

            if "filters" in tmp_output_elements:
                if "user_overridable" in tmp_output_elements["filters"]:
                    if (
                        tmp_output_elements["filters"]["user_overridable"]
                        and not enable_filters
                    ):
                        print(
                            "Deleting " + job + " filters as the user has disabled them"
                        )
                        del tmp_output_elements["filters"]
                    else:
                        print("We cannot disable filters for " + job + "")
                else:
                    print("Deleting " + job + " filters as the user has disabled them")
                    del tmp_output_elements["filters"]

            # if we have any items, lets add the : after the entry
            if tmp_output_elements:
                tmp_output[-1] += ":"
            # lets loop through the elements
            for element in tmp_output_elements:
                if "filters" in element:
                    tmp_output.append(
                        self._common_library.create_space(leading_space + 4)
                        + "filters:"
                    )
                    for element_options in tmp_output_elements[element]:
                        if "user_overridable" in element_options:
                            continue
                        if isinstance(
                            tmp_output_elements[element][element_options], dict
                        ):
                            tmp_output.append(
                                self._common_library.create_space(leading_space + 6)
                                + element_options
                                + ":"
                            )
                            for options_entry in tmp_output_elements[element][
                                element_options
                            ]:
                                tmp_output.append(
                                    self._common_library.create_space(leading_space + 8)
                                    + options_entry
                                    + ":"
                                )
                                for options_subentry in tmp_output_elements[element][
                                    element_options
                                ][options_entry]:
                                    tmp_output.append(
                                        self._common_library.create_space(
                                            leading_space + 10
                                        )
                                        + "- "
                                        + options_subentry
                                        + ""
                                    )

                elif "variations" in element:
                    tmp_output.append(
                        self._common_library.create_space(leading_space + 4) + "matrix:"
                    )
                    tmp_output.append(
                        self._common_library.create_space(leading_space + 6)
                        + "parameters:"
                    )
                    tmp_output.append(
                        self._common_library.create_space(leading_space + 8)
                        + "architecture: "
                        + str(tmp_output_elements[element])
                    )

                elif "context" in element:
                    tmp_output.append(
                        self._common_library.create_space(leading_space + 4)
                        + "context:"
                    )
                    for entry in tmp_output_elements[element]:
                        tmp_output.append(
                            self._common_library.create_space(leading_space + 6)
                            + "- "
                            + entry
                        )

                elif "requires" in element:
                    tmp_output.append(
                        self._common_library.create_space(leading_space + 4)
                        + "requires:"
                    )
                    for require in tmp_output_elements[element]:
                        tmp_output.append(
                            self._common_library.create_space(leading_space + 6)
                            + "- "
                            + require
                        )
                        if require not in workflow_jobs:
                            print(
                                "We shouldn't be here as the following job",
                                require,
                                "doesn't exist in our workflow_jobs",
                            )

                else:
                    print("Problem!!! Not sure how to handle element: ", element)
            if not re.match("^.*merge.*$", job):
                tmp_output.append(
                    self._common_library.create_space(leading_space + 4)
                    + "post-steps:"
                )
                # pr_number = os.environ.get("CIRCLE_PR_NUMBER")
                # if (not pr_number):
                #     tmp_output.append(
                #         self._common_library.create_space(leading_space + 6)
                #         + "- jira/notify"
                #     )

        return tmp_output

    def get_dependency(self, interested_workflow):
        """
        Given a workflow, it will return its dependency
        """
        tmp_output = self.generate_dependency(interested_workflow)

        tmp_output2 = list(set(tmp_output))

        return tmp_output2
