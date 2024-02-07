package com.backend.member;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberRowMapperTest {

    @Test
    void mapRow() throws SQLException {
        // GIVEN row mapper
        MemberRowMapper memberRowMapper = new MemberRowMapper();

        // When
        // mock
        ResultSet resultSet = mock(ResultSet.class);

        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Jake");
        when(resultSet.getString("email")).thenReturn("Jake@corp.ca");
        when(resultSet.getString("password")).thenReturn("password");
        when(resultSet.getInt("age")).thenReturn(21);
        when(resultSet.getString("gender")).thenReturn("MALE");
        when(resultSet.getString("profile_image_id")).thenReturn("45132");

        Member actual = memberRowMapper.mapRow(resultSet, 1);

        // Then assertion
        Member expected = new Member(1, "Jake", "Jake@corp.ca", "password", 21, Gender.MALE, "45132");

        assertThat(actual).isEqualTo(expected);
    }
}