package ratingservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

final class RepositoryTest {

  @Test
  void addRate_shouldInsertAndReturnGeneratedId() throws Exception {
    DataSource dataSource = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement statement = mock(PreparedStatement.class);
    ResultSet generatedKeys = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
        .thenReturn(statement);
    when(statement.executeUpdate()).thenReturn(1);
    when(statement.getGeneratedKeys()).thenReturn(generatedKeys);
    when(generatedKeys.next()).thenReturn(true);
    when(generatedKeys.getLong(1)).thenReturn(77L);

    Repository repository = new Repository(dataSource);
    long rateId = repository.addRate(5L, 3L, 4);

    assertEquals(77L, rateId);

    InOrder order = inOrder(statement, generatedKeys);
    order.verify(statement).setLong(1, 3L);
    order.verify(statement).setLong(2, 5L);
    order.verify(statement).setInt(3, 4);
    order.verify(statement).executeUpdate();
    order.verify(statement).getGeneratedKeys();
    order.verify(generatedKeys).next();
    order.verify(generatedKeys).getLong(1);
  }

  @Test
  void addRate_shouldThrowWhenNoRowsAffected() throws Exception {
    DataSource dataSource = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement statement = mock(PreparedStatement.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
        .thenReturn(statement);
    when(statement.executeUpdate()).thenReturn(0);

    Repository repository = new Repository(dataSource);

    assertThrows(SQLException.class, () -> repository.addRate(1L, 2L, 3));
  }

  @Test
  void addRate_shouldThrowWhenNoGeneratedKey() throws Exception {
    DataSource dataSource = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement statement = mock(PreparedStatement.class);
    ResultSet generatedKeys = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
        .thenReturn(statement);
    when(statement.executeUpdate()).thenReturn(1);
    when(statement.getGeneratedKeys()).thenReturn(generatedKeys);
    when(generatedKeys.next()).thenReturn(false);

    Repository repository = new Repository(dataSource);

    assertThrows(SQLException.class, () -> repository.addRate(1L, 2L, 3));
  }

  @Test
  void getTotalRates_shouldReturnCount() throws Exception {
    DataSource dataSource = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement statement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getLong(1)).thenReturn(12L);

    Repository repository = new Repository(dataSource);
    long count = repository.getTotalRates(9L);

    assertEquals(12L, count);

    InOrder order = inOrder(statement, resultSet);
    order.verify(statement).setLong(1, 9L);
    order.verify(statement).executeQuery();
    order.verify(resultSet).next();
    order.verify(resultSet).getLong(1);
  }

  @Test
  void getRates_shouldReturnCount() throws Exception {
    DataSource dataSource = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement statement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getLong(1)).thenReturn(4L);

    Repository repository = new Repository(dataSource);
    long count = repository.getRates(3L, 11L);

    assertEquals(4L, count);

    InOrder order = inOrder(statement, resultSet);
    order.verify(statement).setLong(1, 3L);
    order.verify(statement).setLong(2, 11L);
    order.verify(statement).executeQuery();
    order.verify(resultSet).next();
    order.verify(resultSet).getLong(1);
  }
}
