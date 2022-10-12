package com.xinbo.sports.apiend.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicParameters;
import com.xinbo.sports.apiend.aop.annotation.UnCheckToken;
import com.xinbo.sports.dao.generator.po.DictItem;
import com.xinbo.sports.dao.generator.service.DictItemService;
import com.xinbo.sports.plat.io.bo.AgLiveRequestParameter;
import com.xinbo.sports.plat.io.bo.AgLiveResponse;
import com.xinbo.sports.plat.service.impl.ae.KingMakerChessServiceImpl;
import com.xinbo.sports.plat.service.impl.ae.SexyLiveServiceImpl;
import com.xinbo.sports.plat.service.impl.ag.AGLiveServiceImpl;
import com.xinbo.sports.plat.service.impl.dg.DGLiveServiceImpl;
import com.xinbo.sports.plat.service.impl.ds.DSChessServiceImpl;
import com.xinbo.sports.plat.service.impl.habanero.HabaneroGameServiceImpl;
import com.xinbo.sports.plat.service.impl.sbo.SBOSportsServiceImpl;
import com.xinbo.sports.plat.service.impl.wm.WMLiveServiceImpl;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.FileUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xinbo.sports.utils.components.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * AG环境测试类
 * </p>
 *
 * @author andy
 * @since 2020/7/14
 */
@Slf4j
@RestController
@RequestMapping("/v1/AgTest")
@Api(tags = "AG环境测试类")
@ApiSort(7)
public class AgTestController {
    @Resource
    private AGLiveServiceImpl aGLiveServiceImpl;
    @Resource
    private DGLiveServiceImpl dGLiveServiceImpl;
    @Resource
    private SBOSportsServiceImpl sboSportsServiceImpl;
    @Resource
    private WMLiveServiceImpl wMAliveServiceImpl;
    @Resource
    private HabaneroGameServiceImpl habaneroGameServiceImpl;
    @Resource
    private DSChessServiceImpl dsChessServiceImpl;
    @Resource
    private DictItemService dictItemServiceImpl;
    @Resource
    private SexyLiveServiceImpl sexyLiveServiceImpl;
    @Resource
    private KingMakerChessServiceImpl kingMakerChessServiceImpl;

    @UnCheckToken
    @ApiOperationSupport(author = "Andy", order = 1)
    @ApiOperation(value = "拉单:获取游戏类型", notes = "拉单:获取游戏类型")
    @PostMapping("/getGameTypes")
    public byte[] getGameTypes(@Valid @RequestBody AgLiveRequestParameter.LanguageDto dto) {
        return aGLiveServiceImpl.dictGameTypes(dto.getLanguage());
    }

    @UnCheckToken
    @ApiOperationSupport(author = "Andy", order = 2)
    @ApiOperation(value = "拉单:获取游戏玩法下注类型", notes = "拉单:获取游戏玩法下注类型")
    @PostMapping("/getGamePlayTypes")
    public byte[] getGamePlayTypes(@Valid @RequestBody AgLiveRequestParameter.LanguageDto dto) {
        return aGLiveServiceImpl.dictGamePlayTypes(dto.getLanguage());
    }

    @UnCheckToken
    @ApiOperationSupport(author = "Andy", order = 3, params = @DynamicParameters(properties =
            {@DynamicParameter(name = "id", value = "主键ID", required = true, example = "200728103051942")}))
    @ApiOperation(value = "拉单:获取游戏结果", notes = "拉单:获取游戏结果")
    @PostMapping("/getRounders")
    public Result<List<AgLiveResponse.RoundersXml>> getRounders(@Valid @RequestBody JSONObject reqBody) {
        Long id = reqBody.getLong("id");
        if (Optional.ofNullable(id).isEmpty()) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        return Result.ok(aGLiveServiceImpl.getRounders(id));
    }

    @UnCheckToken
    @ApiOperationSupport(author = "Andy", order = 4, params = @DynamicParameters(properties =
            {@DynamicParameter(name = "model", value = "WM|DG|AG|SBO|Habanero", required = true, example = "WM")}))
    @ApiOperation(value = "拉单测试", notes = "拉单测试")
    @PostMapping("/pullBetsLips")
    public Result<Boolean> pullBetsLips(@Valid @RequestBody JSONObject reqBody) {
        String model1 = reqBody.getString("model");
        if (Optional.ofNullable(model1).isEmpty()) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        switch (model1) {
            case "AG":
                aGLiveServiceImpl.pullBetsLips();
                break;
            case "DG":
                dGLiveServiceImpl.pullBetsLips();
                break;
            case "WM":
                wMAliveServiceImpl.pullBetsLips();
                break;
            case "SBO":
                sboSportsServiceImpl.pullBetsLips();
                break;
            case "Habanero":
                habaneroGameServiceImpl.pullBetsLips();
                break;
            case "DS":
                dsChessServiceImpl.pullBetsLips();
                break;
            case "Sex":
                sexyLiveServiceImpl.pullBetsLips();
                break;
            case "King":
                kingMakerChessServiceImpl.pullBetsLips();
                break;
            default:
                throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        return Result.ok(true);
    }

    @UnCheckToken
    @ApiOperationSupport(author = "Andy", order = 9)
    @ApiOperation(value = "新增字典项", notes = "新增字典项")
    @PostMapping("/addDictItem")
    public Result<Boolean> addDictItem() {
        Path path = FileUtils.getToUidListFileName();
        try {
            List<String> strings = Files.readAllLines(path);
            List<DictItem> itemList = new ArrayList<>();
            Integer currentTime = DateUtils.getCurrentTime();
            for (String s : strings) {
                String[] split = s.split("=");
                DictItem item = new DictItem();
                item.setCode(split[0]);
                item.setTitle(split[1]);
                item.setCreatedAt(currentTime);
                item.setUpdatedAt(currentTime);
                item.setReferId(106);
                item.setSort(9);
                item.setStatus(1);
                itemList.add(item);
            }
            dictItemServiceImpl.saveBatch(itemList);
        } catch (IOException e) {
            log.error("" + e);
        }
        return Result.ok(true);
    }
}
