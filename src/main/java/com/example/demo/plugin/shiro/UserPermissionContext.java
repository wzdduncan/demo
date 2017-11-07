package com.example.demo.plugin.shiro;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 用户权限上下文辅助类
 * @author wayne
 *
 */
public class UserPermissionContext {
	//系统用户所有权限集合
	private static Map<String,Set> permMap = new HashMap();
	
	//获取用户权限set集合
	public static Set getUserPerms(String userName){
		if(permMap.containsKey(userName)){
			return permMap.get(userName);
		}else{
			return new HashSet();
		}
	}
	
	//用户权限set集合
	public static void addUserPerms(String userName,Set userPermsSet){
		permMap.put(userName, userPermsSet);
	}
	

}
