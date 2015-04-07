__author__ = 'Ralf Quast'

from os import path
from os import walk

from productverifier import ProductVerifier


class SvrRunner:
    def __init__(self, source_dirpath, report_dirpath):
        """
        :param source_dirpath: string
        :param report_dirpath: string
        """
        self.source_dirpath = source_dirpath
        self.report_dirpath = report_dirpath

    def run(self):
        source_pathnames = self.glob_source_pathnames()

        print("Found " + str(len(source_pathnames)) + " source files in '" + self.source_dirpath + "'")

        for source_pathname in source_pathnames:
            print source_pathname
            report_filename = path.basename(source_pathname) + ".json"
            report_pathname = path.join(self.report_dirpath, report_filename)
            verifier = ProductVerifier(source_pathname, report_pathname)
            verifier.verify()

    def glob_source_pathnames(self):
        """

        :rtype : list
        """
        source_pathnames = list()
        for dirpath, dirnames, filenames in walk(self.source_dirpath):
            for filename in filenames:
                if filename.endswith(".nc"):
                    pathname = path.join(dirpath, filename)
                    source_pathnames.append(path.abspath(pathname))
        return source_pathnames

    @staticmethod
    def get_report_filename_pattern():
        """

        :rtype : str
        """
        return '.*\\.nc\\.json'

    @staticmethod
    def get_source_dirpath(archive_root, version, usecase, sensor, year, month):
        """

        :type archive_root: str
        :type version: str
        :type usecase: str
        :type sensor: str
        :type year: str
        :type month: str
        """
        # /<archive-root>/<version>/<usecase>/<sensor>/<yyyy>/<mm>
        return path.join(archive_root, version, usecase, sensor, year, month)

    @staticmethod
    def get_report_dirpath(archive_root, version, usecase, sensor, year=None, month=None):
        """

        :type archive_root: str
        :type version: str
        :type usecase: str
        :type sensor: str
        :type year: str
        :type month: str
        """
        if year is None:
            # /<archive-root>/<version>/<usecase>-svr/<sensor>
            return path.join(archive_root, version, usecase + "-svr", sensor)
        elif month is None:
            # /<archive-root>/<version>/<usecase>-svr/<sensor>/<yyyy>
            return path.join(archive_root, version, usecase + "-svr", sensor, year)
        else:
            # /<archive-root>/<version>/<usecase>-svr/<sensor>/<yyyy>/<mm>
            return path.join(archive_root, version, usecase + "-svr", sensor, year, month)


if __name__ == "__main__":
    import sys

    _year = sys.argv[1]
    _month = sys.argv[2]
    _sensor = sys.argv[3]
    _usecase = sys.argv[4]
    _version = sys.argv[5]
    _archive_root = sys.argv[6]
    _report_root = sys.argv[7]

    source_dirpath_for_month = SvrRunner.get_source_dirpath(_archive_root, _version, _usecase, _sensor, _year, _month)
    report_dirpath_for_month = SvrRunner.get_report_dirpath(_report_root, _version, _usecase, _sensor, _year, _month)
    w = SvrRunner(source_dirpath_for_month, report_dirpath_for_month)

    # noinspection PyBroadException
    try:
        w.run()
    except:
        sys.exit(1)

    sys.exit()