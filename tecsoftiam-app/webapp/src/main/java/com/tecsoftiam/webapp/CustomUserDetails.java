package com.tecsoftiam.webapp;

import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
 
import org.springframework.security.core.GrantedAuthority;

/**
 * UserDettails implementation ->login purpose
 * Author: Deryck Olivier
 */
public class CustomUserDetails implements UserDetails{
 
    private AppUser user;
     
    public CustomUserDetails(AppUser user) {
        this.user = user;
    }
 
    
    /** 
     * @return Collection<? extends GrantedAuthority>
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }
 
    
    /** 
     * @return String
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }
 
    
    /** 
     * @return String
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }
 
    
    /** 
     * @return boolean
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
 
    
    /** 
     * @return boolean
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
 
    
    /** 
     * @return boolean
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
 
    
    /** 
     * @return boolean
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
    
 
}
