package com.giacomelli.RBAC_OAuth2.Service;


import com.auth0.client.mgmt.ManagementApi;
import com.auth0.exception.Auth0Exception;
import com.giacomelli.RBAC_OAuth2.Model.Dto.Request.UsuarioRequestDto;

public interface IAuth0ManagementService {

    ManagementApi getManagementApi() throws Auth0Exception;
    String crearUsuarioEnAuth0(UsuarioRequestDto usuario) throws Auth0Exception;
}
