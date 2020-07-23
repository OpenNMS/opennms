import json
import os
import tempfile
import unittest

from maven import MavenProject, JUnitTest


class TestMaven(unittest.TestCase):

    def setUp(self):
        with open(os.path.join(os.path.dirname(__file__), "sample.json")) as graph_json:
            self.sample_graph = json.load(graph_json)
        # Create a new temporary directory
        self.test_dir = tempfile.TemporaryFile()

    def tearDown(self):
        self.test_dir.close()

    def test_cannot_load_empty_project(self):
        with self.assertRaises(Exception) as context:
            MavenProject({})

    def test_load_project(self):
        project = MavenProject(self.sample_graph)
        self.assertEqual(len(project.modules), 4)
        # First project should be the root module
        self.assertEqual(project.modules[0].is_root(), True)
        # Subsequent module should not be
        self.assertEqual(project.modules[1].is_root(), False)

    def test_get_related_modules(self):
        project = MavenProject(self.sample_graph)
        related = project.get_modules_related_to([os.path.abspath(
            "/home/jesse/git/opennms/opennms-services/src/main/java/org/opennms/netmgt/poller/Poller.java")])
        self.assertEqual(len(related), 1)
        # Grab the first element from the set
        for m in related: break
        self.assertEqual(m.artifact_id, "opennms-services")

    @unittest.skip("requires filesystem access")
    def test_get_modules_for_classnames(self):
        project = MavenProject(self.sample_graph)
        modules = project.get_modules_for_classnames("org.opennms.netmgt.poller.PollerIT")
        self.assertEqual(len(modules), 0)

    def test_get_modules_using(self):
        project = MavenProject(self.sample_graph)
        schema_module = project.get_module("org.opennms.core:org.opennms.core.schema:25.0.0-SNAPSHOT")
        # Verify that the retrieved the correct module
        self.assertEqual(schema_module.artifact_id, "org.opennms.core.schema")

        users = project.get_module_users([schema_module])
        self.assertEqual(len(users), 1)
        # Grab the first element from the set
        for m in users: break
        self.assertEqual(m.artifact_id, "opennms-services")

    def test_classname_from_filename(self):
        class_name = JUnitTest.classname_from_filename("opennms-services/src/test/java/org/opennms/netmgt/poller/PollerIT.java")
        self.assertEqual(class_name, "org.opennms.netmgt.poller.PollerIT")
