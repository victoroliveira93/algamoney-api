package com.example.algamoney.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	@Autowired
	private AuthenticationManager authenticationManager; //Quem pega usuário e senha
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

	    clients.inMemory()
	            .withClient("angular")
	            .secret("$2a$10$TVnipdHOZ/MNKDWTBWdgaOu3cYq1uD8pDE3OV7zXYsJRD0BBROOX.")
	            .scopes("read", "write")
	            .authorizedGrantTypes("password", "refresh_token")
	            .accessTokenValiditySeconds(1800)
	            .refreshTokenValiditySeconds(3600 * 24);
	}
	
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
	    endpoints
	        .tokenStore(tokenStore())
	        .accessTokenConverter(this.accessTokenConverter())
	        .reuseRefreshTokens(false)
	        .userDetailsService(this.userDetailsService)
	        .authenticationManager(this.authenticationManager);
	}
	
	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter accessTokenConverter = new JwtAccessTokenConverter();
		accessTokenConverter.setSigningKey("algaworks"); //senha que valida o token
		return accessTokenConverter;
	}
	
	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}
}

/*
 * Se não colocara anotação Bean, o Spring não vai conseguir gerenciar e injetar este recurso como dependência. Como a classe que utilizamos este bean herda de AuthorizationServerConfigurerAdapter, precisamos seguir as definições já existentes nesta classe.

Você pode dar uma olhada na postagem que fizemos sobre como criar métodos produtores com Spring utilizando @Bean, segue o link: https://blog.algaworks.com/spring-metodos-produtores/

O post e o vídeo irão te ajudar a entender melhor como o @Bean funciona.
 * 
 */
