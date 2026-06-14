package com.example.fitnesstracker.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.example.fitnesstracker.data.entities.Workout;
import java.lang.Class;
import java.lang.Float;
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
public final class WorkoutDao_Impl implements WorkoutDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<Workout> __insertAdapterOfWorkout;

  private final EntityDeleteOrUpdateAdapter<Workout> __deleteAdapterOfWorkout;

  public WorkoutDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfWorkout = new EntityInsertAdapter<Workout>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `workouts` (`id`,`startTime`,`endTime`,`durationSeconds`,`distanceMeters`,`steps`,`caloriesBurned`,`note`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, @NonNull final Workout entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getStartTime());
        statement.bindLong(3, entity.getEndTime());
        statement.bindLong(4, entity.getDurationSeconds());
        statement.bindDouble(5, entity.getDistanceMeters());
        statement.bindLong(6, entity.getSteps());
        statement.bindDouble(7, entity.getCaloriesBurned());
        if (entity.getNote() == null) {
          statement.bindNull(8);
        } else {
          statement.bindText(8, entity.getNote());
        }
      }
    };
    this.__deleteAdapterOfWorkout = new EntityDeleteOrUpdateAdapter<Workout>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `workouts` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, @NonNull final Workout entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final Workout workout, final Continuation<? super Long> $completion) {
    if (workout == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      return __insertAdapterOfWorkout.insertAndReturnId(_connection, workout);
    }, $completion);
  }

  @Override
  public Object delete(final Workout workout, final Continuation<? super Unit> $completion) {
    if (workout == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __deleteAdapterOfWorkout.handle(_connection, workout);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<Workout>> getAll() {
    final String _sql = "SELECT * FROM workouts ORDER BY startTime DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"workouts"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfStartTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "startTime");
        final int _columnIndexOfEndTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "endTime");
        final int _columnIndexOfDurationSeconds = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "durationSeconds");
        final int _columnIndexOfDistanceMeters = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "distanceMeters");
        final int _columnIndexOfSteps = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "steps");
        final int _columnIndexOfCaloriesBurned = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "caloriesBurned");
        final int _columnIndexOfNote = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "note");
        final List<Workout> _result = new ArrayList<Workout>();
        while (_stmt.step()) {
          final Workout _item;
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final long _tmpStartTime;
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime);
          final long _tmpEndTime;
          _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime);
          final long _tmpDurationSeconds;
          _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds);
          final float _tmpDistanceMeters;
          _tmpDistanceMeters = (float) (_stmt.getDouble(_columnIndexOfDistanceMeters));
          final int _tmpSteps;
          _tmpSteps = (int) (_stmt.getLong(_columnIndexOfSteps));
          final float _tmpCaloriesBurned;
          _tmpCaloriesBurned = (float) (_stmt.getDouble(_columnIndexOfCaloriesBurned));
          final String _tmpNote;
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null;
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote);
          }
          _item = new Workout(_tmpId,_tmpStartTime,_tmpEndTime,_tmpDurationSeconds,_tmpDistanceMeters,_tmpSteps,_tmpCaloriesBurned,_tmpNote);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Flow<List<Workout>> getForDay(final long dayStart, final long dayEnd) {
    final String _sql = "SELECT * FROM workouts WHERE startTime >= ? AND startTime < ?";
    return FlowUtil.createFlow(__db, false, new String[] {"workouts"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, dayStart);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, dayEnd);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfStartTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "startTime");
        final int _columnIndexOfEndTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "endTime");
        final int _columnIndexOfDurationSeconds = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "durationSeconds");
        final int _columnIndexOfDistanceMeters = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "distanceMeters");
        final int _columnIndexOfSteps = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "steps");
        final int _columnIndexOfCaloriesBurned = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "caloriesBurned");
        final int _columnIndexOfNote = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "note");
        final List<Workout> _result = new ArrayList<Workout>();
        while (_stmt.step()) {
          final Workout _item;
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final long _tmpStartTime;
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime);
          final long _tmpEndTime;
          _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime);
          final long _tmpDurationSeconds;
          _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds);
          final float _tmpDistanceMeters;
          _tmpDistanceMeters = (float) (_stmt.getDouble(_columnIndexOfDistanceMeters));
          final int _tmpSteps;
          _tmpSteps = (int) (_stmt.getLong(_columnIndexOfSteps));
          final float _tmpCaloriesBurned;
          _tmpCaloriesBurned = (float) (_stmt.getDouble(_columnIndexOfCaloriesBurned));
          final String _tmpNote;
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null;
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote);
          }
          _item = new Workout(_tmpId,_tmpStartTime,_tmpEndTime,_tmpDurationSeconds,_tmpDistanceMeters,_tmpSteps,_tmpCaloriesBurned,_tmpNote);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Flow<Float> totalCaloriesThisWeek(final long weekStart) {
    final String _sql = "SELECT SUM(caloriesBurned) FROM workouts WHERE startTime >= ?";
    return FlowUtil.createFlow(__db, false, new String[] {"workouts"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, weekStart);
        final Float _result;
        if (_stmt.step()) {
          final Float _tmp;
          if (_stmt.isNull(0)) {
            _tmp = null;
          } else {
            _tmp = (float) (_stmt.getDouble(0));
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
  public Flow<List<Long>> workoutDaysThisWeek(final long weekStart) {
    final String _sql = "SELECT startTime FROM workouts WHERE startTime >= ? ORDER BY startTime ASC";
    return FlowUtil.createFlow(__db, false, new String[] {"workouts"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, weekStart);
        final List<Long> _result = new ArrayList<Long>();
        while (_stmt.step()) {
          final Long _item;
          if (_stmt.isNull(0)) {
            _item = null;
          } else {
            _item = _stmt.getLong(0);
          }
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Flow<List<Workout>> workoutsThisWeek(final long weekStart) {
    final String _sql = "SELECT * FROM workouts WHERE startTime >= ?";
    return FlowUtil.createFlow(__db, false, new String[] {"workouts"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, weekStart);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfStartTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "startTime");
        final int _columnIndexOfEndTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "endTime");
        final int _columnIndexOfDurationSeconds = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "durationSeconds");
        final int _columnIndexOfDistanceMeters = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "distanceMeters");
        final int _columnIndexOfSteps = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "steps");
        final int _columnIndexOfCaloriesBurned = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "caloriesBurned");
        final int _columnIndexOfNote = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "note");
        final List<Workout> _result = new ArrayList<Workout>();
        while (_stmt.step()) {
          final Workout _item;
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final long _tmpStartTime;
          _tmpStartTime = _stmt.getLong(_columnIndexOfStartTime);
          final long _tmpEndTime;
          _tmpEndTime = _stmt.getLong(_columnIndexOfEndTime);
          final long _tmpDurationSeconds;
          _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds);
          final float _tmpDistanceMeters;
          _tmpDistanceMeters = (float) (_stmt.getDouble(_columnIndexOfDistanceMeters));
          final int _tmpSteps;
          _tmpSteps = (int) (_stmt.getLong(_columnIndexOfSteps));
          final float _tmpCaloriesBurned;
          _tmpCaloriesBurned = (float) (_stmt.getDouble(_columnIndexOfCaloriesBurned));
          final String _tmpNote;
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null;
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote);
          }
          _item = new Workout(_tmpId,_tmpStartTime,_tmpEndTime,_tmpDurationSeconds,_tmpDistanceMeters,_tmpSteps,_tmpCaloriesBurned,_tmpNote);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM workouts";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
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
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM workouts";
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
