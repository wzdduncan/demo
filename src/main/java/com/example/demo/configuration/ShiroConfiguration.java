package com.example.demo.configuration;


import com.example.demo.plugin.shiro.KickoutSessionControlFilter;
import com.example.demo.plugin.shiro.ShiroRealm;
import com.example.demo.plugin.shiro.URLPermissionsFilter;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * shiro配置类
 * 
 * @author wayne
 */
@Configuration
public class ShiroConfiguration {
	// shiro过滤器链map
	private static Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
	
	/*********shiro session管理************************************/
	/**
	 * 会话ID生成器
	 * @return
	 */
	@Bean
	public JavaUuidSessionIdGenerator getJavaUuidSessionIdGenerator(){
		return new JavaUuidSessionIdGenerator();
	}
	/**
	 * 会话Cookie模板 
	 * @return
	 */
	@Bean
	public SimpleCookie getSimpleCookie(){
		SimpleCookie simpleCookie = new SimpleCookie("sid");
		simpleCookie.setHttpOnly(true);
		simpleCookie.setMaxAge(-1); //maxAge=-1表示浏览器关闭时失效此Cookie
		return simpleCookie;
	}
	/**
	 * 会话DAO
	 * @return
	 */
	@Bean
	public EnterpriseCacheSessionDAO getEnterpriseCacheSessionDAO(){
		EnterpriseCacheSessionDAO cacheSessionDao = new EnterpriseCacheSessionDAO();
		cacheSessionDao.setActiveSessionsCacheName("shiro-activeSessionCache");
		cacheSessionDao.setSessionIdGenerator(getJavaUuidSessionIdGenerator());
		return cacheSessionDao;
	}
	/**
	 *  会话管理器
	 * @return
	 */
	@Bean(name = "sessionManager")
	public DefaultWebSessionManager getDefaultWebSessionManager(){
		DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
		sessionManager.setGlobalSessionTimeout(7200000);
		sessionManager.setDeleteInvalidSessions(true);
		sessionManager.setSessionValidationSchedulerEnabled(true);
		sessionManager.setSessionDAO(getEnterpriseCacheSessionDAO());
		sessionManager.setSessionIdCookieEnabled(true);
		sessionManager.setSessionIdCookie(getSimpleCookie());
		return sessionManager;
	}

	/**
	 * 自定义shiro验证realm
	 * 
	 * @return
	 */
	@Bean(name = "ShiroRealm")
	public ShiroRealm getShiroRealm() {
		return new ShiroRealm();
	}

	/**
	 * shiro缓存管理器
	 * 
	 * @return
	 */
	@Bean(name = "shiroEhcacheManager")
	public EhCacheManager getEhCacheManager() {
		EhCacheManager em = new EhCacheManager();
		em.setCacheManagerConfigFile("classpath:ehcache-shiro.xml");
		return em;
	}

	/**
	 * siro生命周期管理器
	 * 
	 * @return LifecycleBeanPostProcessor
	 */
	@Bean(name = "lifecycleBeanPostProcessor")
	public LifecycleBeanPostProcessor getLifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

	/**
	 * shiro安全管理器
	 * 
	 * @return DefaultWebSecurityManager
	 */
	@Bean(name = "securityManager")
	public DefaultWebSecurityManager getDefaultWebSecurityManager() {
		DefaultWebSecurityManager dwsm = new DefaultWebSecurityManager();
		dwsm.setRealm(getShiroRealm());
		dwsm.setCacheManager(getEhCacheManager());
		dwsm.setSessionManager(getDefaultWebSessionManager());
		return dwsm;
	}
	
	/**
	 * 以下两个为配置shiro注解
	 * @return DefaultAdvisorAutoProxyCreator
	 */
	@Bean
	public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator daap = new DefaultAdvisorAutoProxyCreator();
		daap.setProxyTargetClass(true);
		return daap;
	}
	/**
	 * AuthorizationAttributeSourceAdvisor
	 * @return
	 */
	@Bean
	public AuthorizationAttributeSourceAdvisor getAuthorizationAttributeSourceAdvisor() {
		AuthorizationAttributeSourceAdvisor aasa = new AuthorizationAttributeSourceAdvisor();
		aasa.setSecurityManager(getDefaultWebSecurityManager());
		return new AuthorizationAttributeSourceAdvisor();
	}

	/**
	 * Shiro Filter
	 * 
	 * @return ShiroFilterFactoryBean
	 */
	@Bean(name = "shiroFilterFactoryBean")
	public ShiroFilterFactoryBean getShiroFilterFactoryBean() {
		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
		shiroFilterFactoryBean.setSecurityManager(getDefaultWebSecurityManager());
		shiroFilterFactoryBean.setLoginUrl("/templates/test.html"); // 登录url
		shiroFilterFactoryBean.setSuccessUrl("/templates/test.html"); // 登录成功url
		shiroFilterFactoryBean.setUnauthorizedUrl("/templates/test.html"); // 未授权url
		Map<String, Filter> filters = new HashMap<String, Filter>();
		filters.put("urlperms", getURLPermissionsFilter()); // url权限
		filters.put("kickout", getKickoutSessionControlFilter()); // 并发剔除
		shiroFilterFactoryBean.setFilters(filters);
		shiroFilterFactoryBean.setFilterChainDefinitionMap(new ShiroFilterChain().getFilterChainMap());
		return shiroFilterFactoryBean;
	}

	/******************** 自定义鉴权拦截器 *****************************/
	/**
	 * 基于URL的权限判断过滤器
	 * 
	 * @return URLPermissionsFilter
	 */
	@Bean(name = "URLPermissionsFilter")
	public URLPermissionsFilter getURLPermissionsFilter() {
		return new URLPermissionsFilter();
	}

	/**
	 * 并发剔除 的过滤器
	 * 
	 * @return KickoutSessionControlFilter
	 */
	@Bean(name = "KickoutSessionControlFilter")
	public KickoutSessionControlFilter getKickoutSessionControlFilter() {
		KickoutSessionControlFilter kickoutFilter = new KickoutSessionControlFilter();
		kickoutFilter.setCacheManager(getEhCacheManager());
//		kickoutFilter.setSessionManager(sessionManager);
		kickoutFilter.setKickoutAfter(false); //是否踢出后来登录的，默认是false  
		kickoutFilter.setMaxSession(1);	//同一个用户最大的会话数，默认1 
		kickoutFilter.setKickoutUrl("/templates/test.html");	//被踢出后重定向到的地址  
		kickoutFilter.setSessionManager(getDefaultWebSessionManager());
		return kickoutFilter;
	}
	

	/**
	 * 定义shiro过滤器链
	 * 
	 * @author wayne
	 */
	@Repository
	public class ShiroFilterChain implements ShiroFilterChainDefinition {
		/**
		 * anon:没有参数，表示可以匿名使用。
		 * authc：表示需要认证(登录)才能使用，没有参数。
		 * roles：roles[admin],参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，当有多个参数时，例如admins/user/**=roles["admin,guest"],每个参数通过才算通过，相当于hasAllRoles()方法。
		 * perms：perms[user:add:*],参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，例如/admins/user/**=perms["user:add:*,user:modify:*"]，当有多个参数时必须每个参数都通过才通过，想当于isPermitedAll()方法。
		 * user:没有参数，表示必须存在用户，当登入操作时不做检查。
		 * kickout：自定义，对应KickoutSessionControlFilter。
		 * urlprems：自定义，对应URLPermissionsFilter。
		 */
		@Override
		public Map<String, String> getFilterChainMap() {
			filterChainDefinitionMap.put("/login/**", "anon");
			filterChainDefinitionMap.put("/static/public/**", "anon");
			filterChainDefinitionMap.put("/templates/test.html", "anon");
			filterChainDefinitionMap.put("/**", "user,kickout"); 
			return filterChainDefinitionMap;
		}

	}

}
