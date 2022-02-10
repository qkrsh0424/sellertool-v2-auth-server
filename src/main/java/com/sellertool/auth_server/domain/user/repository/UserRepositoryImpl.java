package com.sellertool.auth_server.domain.user.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sellertool.auth_server.domain.user.entity.QUserEntity;
import com.sellertool.auth_server.domain.user.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory query;

    private final QUserEntity qUserEntity = QUserEntity.userEntity;

    @Autowired
    public UserRepositoryImpl(
            JPAQueryFactory query
    ) {
        this.query = query;
    }

    @Override
    public List<UserEntity> qSelectList(Map<String, Object> params) {
        JPQLQuery customQuery = query.from(qUserEntity)
                .select(
                        qUserEntity
                )
                .where(
                        eqUsername(params),
                        eqEmail(params),
                        eqNickname(params)
                );

        QueryResults<UserEntity> result = customQuery.fetchResults();

        return result.getResults();
    }

    private BooleanExpression eqUsername(Map<String, Object> params) {
        Object usernameObj = params.get("username");
        if (usernameObj == null) {
            return null;
        }

        String username = usernameObj.toString();
        return qUserEntity.username.eq(username);
    }

    private BooleanExpression eqEmail(Map<String, Object> params) {
        Object emailObj = params.get("email");
        if (emailObj == null) {
            return null;
        }

        String email = emailObj.toString();
        return qUserEntity.email.eq(email);
    }

    private BooleanExpression eqNickname(Map<String, Object> params) {
        Object nicknameObj = params.get("nickname");
        if (nicknameObj == null) {
            return null;
        }

        String email = nicknameObj.toString();
        return qUserEntity.nickname.eq(email);
    }
}
