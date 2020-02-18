import errno
import sqlite3
import os

from mdde_stats.service.data import Benchmark


class LocalDataManager:

    def __init__(self, data_dir: str):
        """
        Constructor
        :param data_dir: path to directory where database should be stored
        """
        if data_dir is None:
            raise TypeError('data_dir path was not supplied')

        self._db_name = 'mdde_stats.db'
        self._data_dir = os.path.abspath(data_dir)
        try:
            # Create date folder if not exists
            os.makedirs(self._data_dir)
        except OSError as e:
            if e.errno != errno.EEXIST:
                raise
        self._full_path = os.path.join(self._data_dir, self._db_name)
        self._connection = None

    def initialize_db(self, db_file: str = None):
        """
        Initialize the local db
        :param db_file: (optional) Name of the database file. If not specified, default name is used
        :return:
        """
        if db_file is not None:
            if db_file.isspace():
                raise ValueError("db_file must be a valid file name")
            self._db_name = db_file
            self._full_path = os.path.join(self._data_dir, self._db_name)
        self._make_schema()

    @property
    def db_location(self) -> str:
        """
        Get location of the database file
        :return: String with full path to the database
        """
        return self._full_path

    TABLE_LOGS = 'logs'

    def _make_schema(self):
        """
        Create schema if needed
        """
        conn = sqlite3.connect(self.db_location)
        try:
            with conn:
                c = conn.cursor()
                logs_create_q = 'CREATE TABLE IF NOT EXISTS logs (' \
                                'run_id TEXT NOT NULL, ' \
                                'node_id TEXT, ' \
                                'tuple_id TEXT, ' \
                                'source TEXT, ' \
                                'operation INTEGER NOT NULL, ' \
                                'epoch DATETIME ' \
                                ');'
                logs_run_idx_create_q = 'CREATE INDEX IF NOT EXISTS run_idx ON logs (run_id);'
                logs_run_node_create_q = 'CREATE INDEX IF NOT EXISTS run_node ON logs (node_id);'
                c.execute(logs_create_q)
                c.execute(logs_run_idx_create_q)
                c.execute(logs_run_node_create_q)
        finally:
            conn.close()

    def flush(self):
        """
        Flush all records from the database leaving schema intact
        :return:
        """
        conn = sqlite3.connect(self.db_location)
        try:
            with conn:
                c = conn.cursor()
                c.execute('DELETE FROM {};'.format(self.TABLE_LOGS))
        finally:
            conn.close()


    def __enter__(self) -> Benchmark:
        if self._connection is not None:
            raise RuntimeError("There is already an open connection")
        self._connection = sqlite3.connect(self.db_location)
        return Benchmark(self._connection)

    def __exit__(self, exc_type, exc_value, traceback):
        if exc_type is not None:
            self._connection.rollback()
        else:
            self._connection.commit()
        self._connection.close()
        self._connection = None
