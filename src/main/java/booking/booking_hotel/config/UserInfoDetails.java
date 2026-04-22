package booking.booking_hotel.config;


import booking.booking_hotel.model.Permission;
import booking.booking_hotel.model.Role;
import booking.booking_hotel.model.UserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class UserInfoDetails implements UserDetails {
    private String username;
    private String password;
    private List<GrantedAuthority> authorities;
    private final Set<String> roleNames = new HashSet<>();

    public UserInfoDetails(UserInfo userInfo) {
        username = userInfo.getName();
        password = userInfo.getPassword();
        this.authorities=new ArrayList<>();

        if(userInfo.getRoles()!=null){
            for (Role role : userInfo.getRoles()) {
                if(role.getName()!=null){
                    roleNames.add(role.getName());
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                }
                if(role.getPermissions()!=null){
                    for (Permission permission : role.getPermissions()) {
                        authorities.add(new SimpleGrantedAuthority(permission.getName()));
                    }
                }
            }
        }

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Tài khoản có hết hạn không? (false = hết hạn)
     * Return true = tài khoản còn hiệu lực
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Tài khoản có bị khóa không? (false = bị khóa)
     * Return true = tài khoản không bị khóa
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Mật khẩu có hết hạn không? (false = hết hạn)
     * Return true = mật khẩu còn hiệu lực
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Tài khoản có được kích hoạt không? (false = chưa kích hoạt)
     * Return true = tài khoản đã kích hoạt
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    public Set<String> getRoles() {
        return roleNames;
    }


}
