/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dao;

import model.User;

/**
 *
 * @author ABHILASHM.2010
 */
public interface UserDao {
 
	void save(User user);
	void update(User user);
	void delete(User user);
	User findByUserId(String userId);
 
}
