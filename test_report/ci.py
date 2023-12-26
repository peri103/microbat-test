import xml.etree.ElementTree as ET
from typing import List, Dict
from collections import defaultdict
import json
import pathlib
import re
from functools import reduce

TEST_CLASS_REGEXP = re.compile(r'public\s+.*?class\s+(\w+)\s+')
TEST_CASE_REGEXP = re.compile(r'public\s+.*?void\s+(\w+)\(\)')
MICROBAT_TEST_ROOT_DIR = pathlib.Path('microbat/src/test/microbat')

def extract_last(path: str):
    return path.split('.')[-1]

class TestCase:
    class_name: str
    class_full_name: str
    method_name: str
    time: float
    failure_msg : str | None
    error_msg: str | None
    
    def __init__(self, class_full_name: str, method_name: str, time: float):
        self.class_full_name = class_full_name
        self.class_name = extract_last(class_full_name)
        self.method_name = method_name
        self.time = time
        self.failure_msg = None
        self.error_msg = None
        
    def __str__(self):
        return f'{self.class_name}#{self.method_name}'
    
    def __repr__(self):
        return self.__str__()
    
def asdict_encoder(obj):
    return obj.__dict__

class TestReport:
    cases: List[TestCase]
    suites: List[str]
    suite_cases_map: Dict[str, List[TestCase]]
    
    tests: int
    failures: int
    errors: int
    ignored: int
    
    def _read_and_parse_xml(self, path: str):
        try:
            with open(path, 'r') as f:
                tree = ET.parse(f)
        except:
            print(f'Error: cannot open xml file at {path}, no test report found')
            exit(1)
        root = tree.getroot()
        
        self.tests = int(root.attrib['tests'])
        self.failures = int(root.attrib['failures'])
        self.errors = int(root.attrib['errors'])
        self.ignored = int(root.attrib['ignored'])

        self.cases = []
        self.suites = set()
        self.class_cases_map = defaultdict(list)
        

        for testsuit in root.iter('testsuite'):
            suite_name = testsuit.attrib['name']
            for testcase in testsuit.iter('testcase'):
                case_name = testcase.attrib['name']
                case_suite_name = testcase.attrib['classname']
                assert case_suite_name == suite_name,\
                    'suit name and classname for its case should be the same'
                time = float(testcase.attrib.get('time', 0.0))
                
                case = TestCase(suite_name, case_name, time)
                if testcase.find('failure') is not None:
                    case.failure_msg = testcase.find('failure').text
                if testcase.find('error') is not None:
                    case.error_msg = testcase.find('error').text
                self.cases.append(case)
                self.class_cases_map[extract_last(suite_name)].append(case)
            self.suites.add(extract_last(suite_name))
            
        self.class_cases_map = dict(self.class_cases_map)
        self.suites = list(self.suites)

    def display(self):
        print(f'TestReport tests: {self.tests}, failures: {self.failures}, errors: {self.errors}, ignored: {self.ignored}')
        
        print(json.dumps(self.class_cases_map, indent=4, default=asdict_encoder))
        
    def __init__(self, xml_file_path: str):
        self._read_and_parse_xml(xml_file_path)
        
class MicrobatTest:
    file_path: str
    class_name : str
    case_name: str

    def __init__(self, file_path: str, class_name: str, case_name: str):
        self.file_path = file_path
        self.class_name = class_name
        self.case_name = case_name
        
    def __str__(self):
        return f'{self.class_name}#{self.case_name}'

    def __repr__(self):
        return self.__str__()
        
def parse_microbat_java_file(file_path: pathlib.Path) -> Dict[str, List[MicrobatTest]]:
    with file_path.open('r') as f:
        lines = f.readlines()
        
    i, n = 0, len(lines)
    
    current_test_class_name = None
    test_map = defaultdict(list)

    while i < n and not lines[i].startswith('public class'):
        match_res = TEST_CLASS_REGEXP.match(lines[i])
        if match_res:
            current_test_class_name = TEST_CLASS_REGEXP.match(lines[i]).group(1)
            break
        i += 1

    if i == n:
        print("No test class found in {}".format(file_path))
        return {}
    
    found_test_function = False
    while i < n:
        line = lines[i].strip()
        if line.startswith('@Test'):
            found_test_function = True
            i += 1
            continue

        if found_test_function:
            match_res = TEST_CASE_REGEXP.match(line)
            if match_res:
                test_name = match_res.group(1)
                mt = MicrobatTest(str(file_path.relative_to('.')), current_test_class_name, test_name)
                test_map[current_test_class_name].append(mt)
                found_test_function = False
            i += 1
            continue
        
        match_res = TEST_CLASS_REGEXP.match(line)
        if match_res:
            current_test_class_name = match_res.group(1)
            found_test_function = False
            i += 1
            continue
        i += 1
    return dict(test_map)

def find_microbat_test_cases(root_dir: pathlib.Path) -> Dict[str, List[MicrobatTest]]:
    test_map = {}
    for file_path in root_dir.rglob('*.java'):
        ret = parse_microbat_java_file(file_path)
        test_map.update(ret)
    return test_map

def show_cases_by_kind(report: TestReport, test_map: Dict[str, List[MicrobatTest]], kind: str):
    attr = kind + '_msg'
    
    def get_case_in_map(class_name: str, name: str):
        for case in test_map[class_name]:
            if case.case_name == name:
                return case
        return None
    
    print("List all the {} cases:".format(kind))
    
    all_cases = []
    
    for case in report.cases:
        if getattr(case, attr) is not None:
            a = getattr(case, attr)
            to_print = {
                'class': case.class_name,
                'method': case.method_name,
                'time': case.time,
                'filepath': get_case_in_map(case.class_name, case.method_name).file_path,
            }
            all_cases.append((to_print, a))
    assert(len(all_cases) != 0)
    for case, msg in all_cases:
        print('case: {}'.format(json.dumps(case, indent=4)))
        print('msg: {}'.format(msg))

def show_failed_cases(report: TestReport, test_map: Dict[str, List[MicrobatTest]]):
    show_cases_by_kind(report, test_map, 'failure')

def show_error_cases(report: TestReport, test_map: Dict[str, List[MicrobatTest]]):
    show_cases_by_kind(report, test_map, 'error')

def ensure_test_case_equals(report: TestReport, test_map: Dict[str, List[MicrobatTest]]):
    mb_cases = set([x.__repr__() for x in reduce(lambda x, y: x + y, test_map.values())])
    report_cases = set([x.__repr__() for x in report.cases])
    
    if report_cases != mb_cases:
        print('Error: test cases in report and microbat source code are not equal, please check')
        print('in microbat:\n{}'.format(mb_cases))
        print('in report:\n{}'.format(report_cases))
        exit(1)


def compare_report_and_code(report: TestReport, test_map: Dict[str, List[MicrobatTest]]):
    case_count = sum(len(v) for v in test_map.values())
    print(f'Found {case_count} test cases in microbat source code in total')
    
    if case_count != report.tests:
        print('Error: test case count in report({}) is not equal to that in microbat source code({})'.format(report.tests, case_count))
        exit(1)
        
    ensure_test_case_equals(report, test_map)

    if report.ignored != 0:
        print('Warning: some test cases are ignored')
    
    if report.failures != 0:
        print('Error: some test cases are failed')
        show_failed_cases(report, test_map)
        exit(1)

    if report.errors != 0:
        print('Error: some test cases are errored')
        show_error_cases(report, test_map)
        exit(1) 

    print('All test cases are passed')
    exit(0)

        
def ci_main():
    test_report = TestReport('test_report/report.xml')
    microbat_test_cases = find_microbat_test_cases(MICROBAT_TEST_ROOT_DIR)
    compare_report_and_code(test_report, microbat_test_cases)

ci_main()