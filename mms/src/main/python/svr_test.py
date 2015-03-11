from svr import Svr

import unittest

class SvrTests(unittest.TestCase):

    def setUp(self):
        self.svr = Svr()

    def test_assemble_call(self):
        call = self.svr.assemble_call("python_path", "/input/dir", "/result/dir", "wheater_measure")
        self.assertEquals("python_path svr_workflow.py /input/dir /result/dir wheater_measure", call)

    def test_assemble_input_path(self):
        input_path = self.svr.assemble_input_path("/archive/directory/root", "SensoR", "2008", "05")
        self.assertEquals("/archive/directory/root/SensoR/2008/05", input_path)

if __name__ == '__main__':
    unittest.main()
