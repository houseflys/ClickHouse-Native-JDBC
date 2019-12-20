package com.github.housepower.jdbc.benchmark;

import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 */
public class SelectIBenchmark extends AbstractIBenchmark {
    public WithConnection benchSelect = connection -> {
        long sum = 0;
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(String.format("SELECT number as n1, 'i_am_a_string' , now(), today() from numbers(%d)", selectNumber));
        while (rs.next()) {
            sum += rs.getLong(1);

            rs.getString(2);
            rs.getTimestamp(3);
            rs.getDate(4);
        }
        Assert.assertEquals((selectNumber-1) * selectNumber / 2, sum);
    };

    @Benchmark
    @Test
    public void benchSelectNative() throws Exception {
        withConnection(benchSelect, ConnectionType.NATIVE);
    }

    @Benchmark
    @Test
    public void benchSelectHTTP() throws Exception {
        withConnection(benchSelect, ConnectionType.HTTP);
    }
}
