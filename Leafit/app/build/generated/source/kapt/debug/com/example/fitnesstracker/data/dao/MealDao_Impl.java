package com.example.fitnesstracker.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.example.fitnesstracker.data.entities.Meal;
import java.lang.Class;
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
public final class MealDao_Impl implements MealDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<Meal> __insertAdapterOfMeal;

  private final EntityDeleteOrUpdateAdapter<Meal> __deleteAdapterOfMeal;

  public MealDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfMeal = new EntityInsertAdapter<Meal>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `meals` (`id`,`date`,`mealType`,`name`,`calories`,`proteins`,`fats`,`carbs`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, @NonNull final Meal entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getDate());
        if (entity.getMealType() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getMealType());
        }
        if (entity.getName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getName());
        }
        statement.bindDouble(5, entity.getCalories());
        statement.bindDouble(6, entity.getProteins());
        statement.bindDouble(7, entity.getFats());
        statement.bindDouble(8, entity.getCarbs());
      }
    };
    this.__deleteAdapterOfMeal = new EntityDeleteOrUpdateAdapter<Meal>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `meals` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, @NonNull final Meal entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final Meal meal, final Continuation<? super Long> $completion) {
    if (meal == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      return __insertAdapterOfMeal.insertAndReturnId(_connection, meal);
    }, $completion);
  }

  @Override
  public Object delete(final Meal meal, final Continuation<? super Unit> $completion) {
    if (meal == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __deleteAdapterOfMeal.handle(_connection, meal);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<Meal>> getMealsForDay(final long dayStart, final long dayEnd) {
    final String _sql = "SELECT * FROM meals WHERE date >= ? AND date < ? ORDER BY date ASC";
    return FlowUtil.createFlow(__db, false, new String[] {"meals"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, dayStart);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, dayEnd);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfDate = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "date");
        final int _columnIndexOfMealType = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "mealType");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfCalories = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "calories");
        final int _columnIndexOfProteins = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "proteins");
        final int _columnIndexOfFats = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "fats");
        final int _columnIndexOfCarbs = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "carbs");
        final List<Meal> _result = new ArrayList<Meal>();
        while (_stmt.step()) {
          final Meal _item;
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final long _tmpDate;
          _tmpDate = _stmt.getLong(_columnIndexOfDate);
          final String _tmpMealType;
          if (_stmt.isNull(_columnIndexOfMealType)) {
            _tmpMealType = null;
          } else {
            _tmpMealType = _stmt.getText(_columnIndexOfMealType);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final float _tmpCalories;
          _tmpCalories = (float) (_stmt.getDouble(_columnIndexOfCalories));
          final float _tmpProteins;
          _tmpProteins = (float) (_stmt.getDouble(_columnIndexOfProteins));
          final float _tmpFats;
          _tmpFats = (float) (_stmt.getDouble(_columnIndexOfFats));
          final float _tmpCarbs;
          _tmpCarbs = (float) (_stmt.getDouble(_columnIndexOfCarbs));
          _item = new Meal(_tmpId,_tmpDate,_tmpMealType,_tmpName,_tmpCalories,_tmpProteins,_tmpFats,_tmpCarbs);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Flow<List<Long>> getAvailableDates() {
    final String _sql = "SELECT DISTINCT date FROM meals ORDER BY date DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"meals"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
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
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM meals";
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
