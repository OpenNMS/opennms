### All Contributors

* [ ] Have you read our [Contribution Guidelines](https://github.com/OpenNMS/opennms/blob/main/CONTRIBUTING.md)?
* [ ] Have you (electronically) signed the [OpenNMS Contributor Agreement](https://cla-assistant.io/OpenNMS/opennms)?

### Contribution Checklist

* Please [make an issue in the OpenNMS issue tracker](https://opennms.atlassian.net/) if there isn't one already.<br />Once there is an issue, please:
  1. update the title of this PR to be in the format: `${JIRA-ISSUE-NUMBER}: subject of pull request`
  2. update the Jira link at the bottom of this comment to refer to the real issue number
  3. prefix your commit messages with the issue number, if possible
  4. once you've created this PR, please link to it in a comment in the Jira issue
  Don't worry if this sounds like a lot, we can help you get things set up properly.
* **If this code is likely to affect the UI, did you name your branch with `-smoke` in it to trigger smoke tests?**
* If this is a new or updated feature, is there documentation for the new behavior?
* If this is new code, are there unit and/or integration tests?
* If this PR targets a `foundation-*` branch, does it try to avoid changing files in `$OPENNMS_HOME/etc/`?

### What's Next?

A PR should be assigned at least 2 reviewers.  If you know that someone would be a good person to review your code, feel free to add them.

If you need help making additions or changes to the documentation related to your changes, please let us know.

In any case, if anything is unclear or you want help getting your PR ready for merge, please don't hesitate to say something in the comments here,
or in [the #opennms-development chat channel](https://chat.opennms.com/opennms/channels/opennms-development).

Once reviewer(s) accept the PR and the branch passes continuous integration, the PR is eligible for merge.

At that time, if you have commit access (are an OpenNMS Group employee or a member of the OGP) you are welcome to merge the PR when you're ready.
Otherwise, a reviewer can merge it for you.

Thanks for taking time to contribute!

### External References

* Jira (Issue Tracker): https://opennms.atlassian.net/browse/${JIRA-ISSUE-NUMBER}

