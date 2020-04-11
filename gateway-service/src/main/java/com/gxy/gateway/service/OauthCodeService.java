package com.gxy.gateway.service;

import com.gxy.gateway.client.domain.OauthCodeDO;
import com.gxy.gateway.client.query.OauthCodeQueryDO;
import com.gxy.service.base.BaseServiceAO;

/**
 * 在项目中,主要操作oauth_code表的对象是JdbcAuthorizationCodeServices.java. 更多的细节请参考该类.
只有当grant_type为"authorization_code"时,该表中才会有数据产生; 其他的grant_type没有使用该表.
 *
 * @author guoxiaoyu
 * @email ggg_xiaoyu@163.com
 * @date 2020-04-05 19:35:19
 */
public interface OauthCodeService extends BaseServiceAO<OauthCodeDO,OauthCodeQueryDO> {

}
