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
  void getRatingSummary_shouldReturnAggregatedCounts() throws Exception {
    DataSource dataSource = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement statement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getLong("total")).thenReturn(12L);
    when(resultSet.getLong("rate1")).thenReturn(2L);
    when(resultSet.getLong("rate2")).thenReturn(3L);
    when(resultSet.getLong("rate3")).thenReturn(4L);
    when(resultSet.getLong("rate4")).thenReturn(1L);
    when(resultSet.getLong("rate5")).thenReturn(2L);

    Repository repository = new Repository(dataSource);
    ratingservice.model.Data data = repository.getRatingSummary(11L);

    assertEquals(12L, data.getTotalRates());
    assertEquals(2L, data.getRate1Count());
    assertEquals(3L, data.getRate2Count());
    assertEquals(4L, data.getRate3Count());
    assertEquals(1L, data.getRate4Count());
    assertEquals(2L, data.getRate5Count());

    InOrder order = inOrder(statement, resultSet);
    order.verify(statement).setInt(1, ratingservice.constants.Constants.RATE_1);
    order.verify(statement).setInt(2, ratingservice.constants.Constants.RATE_2);
    order.verify(statement).setInt(3, ratingservice.constants.Constants.RATE_3);
    order.verify(statement).setInt(4, ratingservice.constants.Constants.RATE_4);
    order.verify(statement).setInt(5, ratingservice.constants.Constants.RATE_5);
    order.verify(statement).setLong(6, 11L);
    order.verify(statement).executeQuery();
    order.verify(resultSet).next();
    order.verify(resultSet).getLong("total");
  }

  @Test
  void getRatingSummary_shouldReturnZeroDataWhenNoRows() throws Exception {
    DataSource dataSource = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement statement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);

    Repository repository = new Repository(dataSource);
    ratingservice.model.Data data = repository.getRatingSummary(42L);

    assertEquals(0L, data.getTotalRates());
    assertEquals(0L, data.getRate1Count());
    assertEquals(0L, data.getRate2Count());
    assertEquals(0L, data.getRate3Count());
    assertEquals(0L, data.getRate4Count());
    assertEquals(0L, data.getRate5Count());
  }
}
