package com.example.fitnesstracker.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.example.fitnesstracker.data.entities.WaterRecord;
import java.lang.Class;
import java.lang.Integer;
import java.lang.Long;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class WaterDao_Impl implements WaterDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<WaterRecord> __insertAdapterOfWaterRecord;

  private final EntityDeleteOrUpdateAdapter<WaterRecord> __deleteAdapterOfWaterRecord;

  public WaterDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfWaterRecord = new EntityInsertAdapter<WaterRecord>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `water_records` (`id`,`timestamp`,`amountMl`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final WaterRecord entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindLong(3, entity.getAmountMl());
      }
    };
    this.__deleteAdapterOfWaterRecord = new EntityDeleteOrUpdateAdapter<WaterRecord>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `water_records` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final WaterRecord entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final WaterRecord record, final Continuation<? super Long> $completion) {
    if (record == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      return __insertAdapterOfWaterRecord.insertAndReturnId(_connection, record);
    }, $completion);
  }

  @Override
  public Object delete(final WaterRecord record, final Continuation<? super Unit> $completion) {
    if (record == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __deleteAdapterOfWaterRecord.handle(_connection, record);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<WaterRecord>> getForDay(final long dayStart, final long dayEnd) {
    final String _sql = "SELECT * FROM water_records WHERE timestamp >= ? AND timestamp < ? ORDER BY timestamp ASC";
    return FlowUtil.createFlow(__db, false, new String[] {"water_records"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, dayStart);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, dayEnd);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfTimestamp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "timestamp");
        final int _columnIndexOfAmountMl = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "amountMl");
        final List<WaterRecord> _result = new ArrayList<WaterRecord>();
        while (_stmt.step()) {
          final WaterRecord _item;
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final long _tmpTimestamp;
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp);
          final int _tmpAmountMl;
          _tmpAmountMl = (int) (_stmt.getLong(_columnIndexOfAmountMl));
          _item = new WaterRecord(_tmpId,_tmpTimestamp,_tmpAmountMl);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Flow<Integer> totalMlForDay(final long dayStart, final long dayEnd) {
    final String _sql = "SELECT SUM(amountMl) FROM water_records WHERE timestamp >= ? AND timestamp < ?";
    return FlowUtil.createFlow(__db, false, new String[] {"water_records"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, dayStart);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, dayEnd);
        final Integer _result;
        if (_stmt.step()) {
          final Integer _tmp;
          if (_stmt.isNull(0)) {
            _tmp = null;
          } else {
            _tmp = (int) (_stmt.getLong(0));
          }
          _result = _tmp;
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Object getLatestToday(final long dayStart, final long dayEnd,
      final Continuation<? super WaterRecord> $completion) {
    final String _sql = "SELECT * FROM water_records WHERE timestamp >= ? AND timestamp < ? ORDER BY timestamp DESC LIMIT 1";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, dayStart);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, dayEnd);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfTimestamp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "timestamp");
        final int _columnIndexOfAmountMl = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "amountMl");
        final WaterRecord _result;
        if (_stmt.step()) {
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final long _tmpTimestamp;
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp);
          final int _tmpAmountMl;
          _tmpAmountMl = (int) (_stmt.getLong(_columnIndexOfAmountMl));
          _result = new WaterRecord(_tmpId,_tmpTimestamp,_tmpAmountMl);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM water_records";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
