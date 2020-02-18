import sqlite3
from typing import Sequence, Union

from .event import LogEvent


class Benchmark:

    def __init__(self, db_connection: sqlite3.Connection):
        if db_connection is None:
            raise TypeError("db_connection can't be None")
        self._db_connection = db_connection
        self._cursor = db_connection.cursor()

    def insert_event(self, event: LogEvent):
        self._cursor.execute('INSERT INTO logs (run_id, node_id, tuple_id, source, operation, epoch) '
                             'VALUES (?, ?, ?, ?, ?, ?)',
                             (event.run_id, event.node_id, event.tuple_id, event.source, event.action, event.epoch))

    def insert_events(self, events: Union[LogEvent, Sequence[LogEvent]]):
        logs = []
        if isinstance(events, Sequence):
            for e in events:
                logs.append((e.run_id, e.node_id, e.tuple_id, e.source, e.action, e.epoch))
            self._cursor.executemany('INSERT INTO logs (run_id, node_id, tuple_id, source, operation, epoch) '
                                     'VALUES (?, ?, ?, ?, ?)', logs)
        else:
            self.insert_event(events)

    def get_run_nodes(self, run_id: str):
        self._cursor.execute("SELECT DISTINCT node_id FROM logs WHERE run_id = ?;", (run_id, ))
        return self._cursor.fetchall()


    def commit(self):
        if self._db_connection is None:
            raise RuntimeError("There is no open connection")
        self._db_connection.commit()
