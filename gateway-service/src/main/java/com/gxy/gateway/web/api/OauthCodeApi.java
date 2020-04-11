package com.gxy.gateway.web.api;

import com.gxy.client.base.CommonResult;
import com.gxy.gateway.client.domain.OauthCodeDO;
import com.gxy.gateway.client.query.OauthCodeQueryDO;
import com.gxy.gateway.service.OauthCodeService;
import com.gxy.service.base.BaseControllerImpl;
import com.gxy.service.base.BaseServiceAO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在项目中,主要操作oauth_code表的对象是JdbcAuthorizationCodeServices.java. 更多的细节请参考该类.
只有当grant_type为"authorization_code"时,该表中才会有数据产生; 其他的grant_type没有使用该表.
 *
 * @author guoxiaoyu
 * @email ggg_xiaoyu@163.com
 * @date 2020-04-05 19:35:19
 */
@Api()
@Slf4j
@RestController
@RequestMapping("/api/oauthcode")
public class OauthCodeApi extends BaseControllerImpl<OauthCodeDO, OauthCodeQueryDO> {

    @Autowired
    private OauthCodeService baseService;

    @Override
    public BaseServiceAO<OauthCodeDO, OauthCodeQueryDO> getService() {
        return baseService;
    }
    /**
     * 通用查询逻辑
     *
     * @param q        查询对象
     * @return
     */
    @ApiOperation(value = "通用查询逻辑", httpMethod = "GET", notes = "通用查询逻辑")
    @RequestMapping("query")
    public CommonResult<List<OauthCodeDO>> select(@ModelAttribute("pojo") OauthCodeQueryDO q) {
        CommonResult<List<OauthCodeDO>> query = getService().query(q);
        CommonResult<Integer> count = getService().count(q);
        query.setTotal(count.getResult());
        return query;
    }

    /**
     * 获取详情
     *
     * @return
     */
    @ApiOperation(value = "获取详情", httpMethod = "GET", notes = "获取详情")
    @RequestMapping("get")
    public CommonResult<OauthCodeDO> detail(@RequestParam("id") Long id) {
        return getService().get(id);
    }

    /**
     * 通用更新逻辑
     *
     * @param t
     * @return
     */
    @ApiOperation(value = "通用更新逻辑", httpMethod = "GET", notes = "通用更新逻辑")
    @RequestMapping("update")
    public CommonResult<Long> update(@ModelAttribute("pojo") OauthCodeDO t) {
        return getService().modify(t);
    }

    /**
     * 通用删除逻辑
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "通用删除逻辑", httpMethod = "GET", notes = "通用删除逻辑")
    @RequestMapping("delete")
    public CommonResult<Long> delete(@RequestParam("id") Long id) {
        return getService().remove(id);
    }

    /**
     * 通用插入逻辑
     *
     * @param t
     * @return
     */
    @ApiOperation(value = "通用插入逻辑", httpMethod = "POST", notes = "通用插入逻辑")
    @RequestMapping("add")
    public CommonResult<OauthCodeDO> insert(@RequestBody OauthCodeDO t) {
        return getService().save(t);
    }
}
