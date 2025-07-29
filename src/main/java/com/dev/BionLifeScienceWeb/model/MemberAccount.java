package com.dev.BionLifeScienceWeb.model;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import lombok.Getter;


@Getter
public class MemberAccount extends User{
	
	private static final long serialVersionUID = 1L;
	
	private Member member;

    public MemberAccount(Member member) {

        super(
        		member.getUsername(), 
        		member.getPassword(),
                getAuthorities(member)
             );
        this.member = member;
        System.out.println(member.toString());
    }

    /**
     * 권한 받아오는 부분
     * @param account
     * @return
     */
    private static Collection<? extends GrantedAuthority> getAuthorities(Member member) {
        String[] userRoles = new String[1];
        userRoles[0] = member.getRole();
        System.out.println(userRoles[0]);
        Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(userRoles);
        return authorities;
    }
}
