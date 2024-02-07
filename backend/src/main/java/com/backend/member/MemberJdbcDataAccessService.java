package com.backend.member;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("jdbc")
public class MemberJdbcDataAccessService implements MemberDao {

    private final JdbcTemplate jdbcTemplate;
    private final MemberRowMapper memberRowMapper;

    public MemberJdbcDataAccessService(JdbcTemplate jdbcTemplate, MemberRowMapper memberRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.memberRowMapper = memberRowMapper;
    }

    @Override
    public List<Member> selectAllMembers() {
        var sql = """
                SELECT id, name, email, password, age, gender, profile_image_id
                FROM member
                LIMIT 1000
                """;

        return jdbcTemplate.query(sql, memberRowMapper);
    }

    @Override
    public Optional<Member> selectMemberById(Integer id) {
        var sql = """
                SELECT id, name, email, password, age, gender
                FROM member
                WHERE id = ?
                """;

        return jdbcTemplate.query(sql, memberRowMapper, id)
                .stream().findFirst();
    }

    @Override
    public void insertMember(Member member) {
        var sql = """
                INSERT INTO member(name, email, password, age, gender)
                VALUES (?,?,?,?,?,?)
                """;
        jdbcTemplate.update(sql,
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                member.getAge(),
                member.getGender().name()
        );
    }

    @Override
    public void deleteMemberById(Integer id) {
        var sql = """
                DELETE
                FROM member
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void updateMember(Member member) {
        if(member.getName() != null){
            String query = "Update member SET name = ? WHERE id = ?";
            jdbcTemplate.update(query,
                    member.getName(),
                    member.getId());
        }
        if(member.getAge() == 0){
            String query = "Update member SET age = ? WHERE id = ?";
            jdbcTemplate.update(query,
                    member.getAge(),
                    member.getId());
        }
        if(member.getEmail() != null){
            String query = "Update member SET email = ? WHERE id = ?";
            jdbcTemplate.update(query,
                    member.getEmail(),
                    member.getId());
        }
    }

    @Override
    public boolean existsMemberWithEmail(String email) {
        var sql = """
                SELECT count(id)
                FROM member
                WHERE email = ?
                """;
        int count = jdbcTemplate.queryForObject(sql, int.class, email);
        return count > 0;
    }

    @Override
    public boolean existsMemberWithId(Integer id) {
        var sql = """
                SELECT count(id)
                FROM member
                WHERE id = ?
                """;
        int count = jdbcTemplate.queryForObject(sql, int.class, id);
        return count > 0;
    }

    @Override
    public Optional<Member> selectUserByEmail(String email) {
        var sql = """
                SELECT id, name, email, password, age, gender, profile_image_id
                FROM member
                WHERE email = ?
                """;

        return jdbcTemplate.query(sql, memberRowMapper, email)
                .stream().findFirst();
    }

    @Override
    public void updateMemberProfileImageId(String profileImageId, Integer id) {
        var sql = """
                UPDATE member
                SET profile_image_id = ?
                where id = ?
                """;
        jdbcTemplate.update(sql, profileImageId, id);
    }
}
