package com.example.fitnesstracker.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.example.fitnesstracker.data.entities.UserProfile;
import java.lang.Class;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class UserProfileDao_Impl implements UserProfileDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<UserProfile> __insertAdapterOfUserProfile;

  public UserProfileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfUserProfile = new EntityInsertAdapter<UserProfile>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_profile` (`id`,`name`,`gender`,`age`,`heightCm`,`weightKg`,`avatarUri`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final UserProfile entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getName());
        }
        if (entity.getGender() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getGender());
        }
        statement.bindLong(4, entity.getAge());
        statement.bindDouble(5, entity.getHeightCm());
        statement.bindDouble(6, entity.getWeightKg());
        if (entity.getAvatarUri() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getAvatarUri());
        }
      }
    };
  }

  @Override
  public Object upsert(final UserProfile profile, final Continuation<? super Unit> $completion) {
    if (profile == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfUserProfile.insert(_connection, profile);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<UserProfile> getProfile() {
    final String _sql = "SELECT * FROM user_profile WHERE id = 1";
    return FlowUtil.createFlow(__db, false, new String[] {"user_profile"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfGender = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "gender");
        final int _columnIndexOfAge = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "age");
        final int _columnIndexOfHeightCm = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "heightCm");
        final int _columnIndexOfWeightKg = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "weightKg");
        final int _columnIndexOfAvatarUri = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "avatarUri");
        final UserProfile _result;
        if (_stmt.step()) {
          final int _tmpId;
          _tmpId = (int) (_stmt.getLong(_columnIndexOfId));
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final String _tmpGender;
          if (_stmt.isNull(_columnIndexOfGender)) {
            _tmpGender = null;
          } else {
            _tmpGender = _stmt.getText(_columnIndexOfGender);
          }
          final int _tmpAge;
          _tmpAge = (int) (_stmt.getLong(_columnIndexOfAge));
          final float _tmpHeightCm;
          _tmpHeightCm = (float) (_stmt.getDouble(_columnIndexOfHeightCm));
          final float _tmpWeightKg;
          _tmpWeightKg = (float) (_stmt.getDouble(_columnIndexOfWeightKg));
          final String _tmpAvatarUri;
          if (_stmt.isNull(_columnIndexOfAvatarUri)) {
            _tmpAvatarUri = null;
          } else {
            _tmpAvatarUri = _stmt.getText(_columnIndexOfAvatarUri);
          }
          _result = new UserProfile(_tmpId,_tmpName,_tmpGender,_tmpAge,_tmpHeightCm,_tmpWeightKg,_tmpAvatarUri);
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
  public Object clear(final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM user_profile";
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
