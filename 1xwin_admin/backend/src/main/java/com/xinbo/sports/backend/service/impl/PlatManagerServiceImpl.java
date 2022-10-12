package com.xinbo.sports.backend.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinbo.sports.backend.io.bo.PlatManager;
import com.xinbo.sports.backend.io.dto.Platform;
import com.xinbo.sports.backend.mapper.PlatManagerMapper;
import com.xinbo.sports.backend.redis.GameCache;
import com.xinbo.sports.backend.service.IPlatManagerService;
import com.xinbo.sports.dao.generator.po.GameList;
import com.xinbo.sports.dao.generator.po.GameSlot;
import com.xinbo.sports.dao.generator.po.TeamLogo;
import com.xinbo.sports.dao.generator.service.GameListService;
import com.xinbo.sports.dao.generator.service.GameSlotService;
import com.xinbo.sports.dao.generator.service.PlatListService;
import com.xinbo.sports.dao.generator.service.TeamLogoService;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.factory.PlatSportsAbstractFactory;
import com.xinbo.sports.plat.io.dto.base.BetslipsDetailDto;
import com.xinbo.sports.plat.service.impl.bti.BTISportsServiceImpl;
import com.xinbo.sports.service.base.NoticeBase;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.FastDFSClientUtils;
import com.xinbo.sports.utils.JedisUtil;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.alibaba.fastjson.JSON.*;
import static com.xinbo.sports.service.cache.KeyConstant.COMMON_TOTAL_HASH;
import static com.xinbo.sports.service.cache.KeyConstant.SPORT_SCHEDULE;
import static java.util.Objects.nonNull;

/**
 * <p>
 * 游戏管理->平台管理
 * </p>
 *
 * @author andy
 * @since 2020/6/8
 */
@Slf4j
@Service
public class PlatManagerServiceImpl extends ServiceImpl<PlatManagerMapper, PlatManager.ListPlatReqBody> implements IPlatManagerService {
    @Resource
    private PlatListService platListServiceImpl;
    @Resource
    private GameListService gameListServiceImpl;
    @Resource
    private GameSlotService gameSlotServiceImpl;
    @Resource
    private GameCache gameCache;
    @Resource
    private ConfigCache configCache;
    @Resource
    private NoticeBase noticeBase;
    @Resource
    private JedisUtil jedisUtil;
    @Resource
    private TeamLogoService teamLogoServiceImpl;
    @Resource
    private FastDFSClientUtils fastDFSClientUtils;
    @Resource
    private BTISportsServiceImpl btiSportsServiceImpl;


    private static final String MODEL = "BTI";

    @Override
    public ResPage<PlatManager.ListPlatResBody> listPlat(ReqPage<PlatManager.ListPlatReqBody> reqBody) {
        ResPage<PlatManager.ListPlatResBody> resPage = null;
        if (nonNull(reqBody)) {
            PlatManager.ListPlatReqBody data = reqBody.getData();
            var wrapper = new QueryWrapper<GameList>();
            if (data != null) {
                wrapper.likeRight(nonNull(data.getCode()), "name", data.getCode());
                wrapper.eq(nonNull(data.getName()), "name", data.getName());
                wrapper.eq(nonNull(data.getStatus()), "status", data.getStatus());
            }
            var page = gameListServiceImpl.page(reqBody.getPage(), wrapper);
            DecimalFormat df = new DecimalFormat("0.00");
            var listPlatResBodyPage = BeanConvertUtils.copyPageProperties(page, PlatManager.ListPlatResBody::new, (sb, bo) -> {
                bo.setCode(parseObject(sb.getModel()).getString("parent"));
                bo.setMaintenance(!parseObject(sb.getMaintenance()).isEmpty() ? parseObject(sb.getMaintenance()) : new JSONObject());
                bo.setRevenueRate(df.format(sb.getRevenueRate().multiply(BigDecimal.valueOf(100))) + "%");

            });
            resPage = ResPage.get(listPlatResBodyPage);
        }
        return resPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlat(PlatManager.UpdatePlatReqBody reqBody) {
        if (nonNull(reqBody) && nonNull(reqBody.getId())) {
            var maintenance = nonNull(reqBody.getMaintenance()) && reqBody.getStatus() != 0 ? reqBody.getMaintenance() : "{}";
            boolean update = gameListServiceImpl.lambdaUpdate().set(nonNull(reqBody.getSort()), GameList::getSort, reqBody.getSort())
                    .set(nonNull(reqBody.getStatus()), GameList::getStatus, reqBody.getStatus())
                    .set(GameList::getMaintenance, maintenance)
                    .set(GameList::getUpdatedAt, DateNewUtils.now())
                    .eq(GameList::getId, reqBody.getId())
                    .update();

            if (update) {
                noticeBase.writeMaintainInfo(reqBody.getId());
                gameCache.updateGameListCache(reqBody.getId());
                gameCache.updateGameListCache(reqBody.getId());
                gameCache.updateGroupGameListCache();
                gameCache.delGameListCache();
                gameCache.delGamePropCache(reqBody.getId());
            } else {
                throw new BusinessException(CodeInfo.UPDATE_ERROR);
            }

        }
    }

    /**
     * 平台子游戏管理-列表
     *
     * @param dto 入参
     * @return 子游戏列表
     */
    @Override
    public ResPage<PlatManager.ListSubGameResBody> listSubGame(ReqPage<PlatManager.ListSubGameReqBody> dto) {
        if (null == dto) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }

        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();

        LambdaQueryChainWrapper<GameSlot> query = gameSlotServiceImpl.lambdaQuery();
        // 状态过滤
        if (dto.getData().getStatus() != null) {
            query.eq(GameSlot::getStatus, dto.getData().getStatus());
        }
        // 显示过滤
        if (dto.getData().getDevice() != null && dto.getData().getDevice() != 0) {
            query.eq(GameSlot::getDevice, dto.getData().getDevice());
        }
        // 游戏类型过滤
        if (dto.getData().getGameId() != null && dto.getData().getGameId() != 0) {
            query.eq(GameSlot::getGameId, dto.getData().getGameId());
        }
        // 游戏名称过滤
        if (StringUtils.isNotBlank(dto.getData().getName())) {
            if (headerInfo.getLang().equals(BaseEnum.LANG.ZH.getValue())) {
                query.likeRight(GameSlot::getNameZh, dto.getData().getName());
            } else {
                query.likeRight(GameSlot::getName, dto.getData().getName());
            }
        }

        // 获取老虎机游戏ID->名字
        List<Platform.GameListInfo> gameListResCache = gameCache.getGameListResCache();
        Map<Integer, String> collect = gameListResCache.stream().filter(gameListInfo -> gameListInfo.getGroupId().equals(2))
                .collect(Collectors.toMap(Platform.GameListInfo::getId, Platform.GameListInfo::getName));


        // 分页数据结果集
        Page<GameSlot> page = query.page(dto.getPage());
        Page<PlatManager.ListSubGameResBody> resData = BeanConvertUtils.copyPageProperties(
                page,
                PlatManager.ListSubGameResBody::new,
                (ori, dest) -> {
                    dest.setName(headerInfo.getLang().equals(BaseEnum.LANG.ZH.getValue()) ? ori.getNameZh() : ori.getName());
                    dest.setGamePlat(collect.getOrDefault(ori.getGameId(), ""));
                }
        );

        return ResPage.get(resData);
    }

    @Override
    public void updateSubGame(PlatManager.UpdateSubGameReqBody reqBody) {
        GameSlot one = gameSlotServiceImpl.lambdaQuery().eq(GameSlot::getId, reqBody.getId()).one();
        if (null == one) {
            throw new BusinessException(CodeInfo.RECORD_NOT_EXIST);
        }

        BeanConvertUtils.beanCopy(reqBody, one);
        one.setUpdatedAt((int) Instant.now().getEpochSecond());
        gameSlotServiceImpl.updateById(one);
    }

    @Override
    public List<PlatManager.GameListResBody> gameList() {
        LambdaQueryWrapper<GameList> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(GameList::getStatus, 1);
        wrapper.orderByAsc(GameList::getId);
        return BeanConvertUtils.copyListProperties(gameListServiceImpl.list(wrapper), PlatManager.GameListResBody::new);
    }

    /**
     * 热门赛事配置列表
     *
     * @param dto 请求参数
     * @return 分页数据
     */
    @Override
    public ResPage<BetslipsDetailDto.SportSchedule> sportCompetitionList(ReqPage<BetslipsDetailDto.SportReqDto> dto) {
        var schedules = jedisUtil.hget(MODEL + SPORT_SCHEDULE, COMMON_TOTAL_HASH);
        var reqDate = dto.getData();
        var sportSchedulesList = parseArray(schedules, BetslipsDetailDto.SportSchedule.class);
        //过滤小于当前时间记录
        sportSchedulesList = Optional.ofNullable(sportSchedulesList).map(list -> list.stream()
                .filter(x -> x.getTimestamp() > DateNewUtils.now()).collect(Collectors.toList()))
                .orElse(new ArrayList<>());
        //无记录拉取三方记录
        if (CollectionUtils.isEmpty(sportSchedulesList)) {
            var reqPage = new ReqPage<BetslipsDetailDto.SportScheduleReqDto>();
            List<GameList> list = gameListServiceImpl.lambdaQuery().ne(GameList::getStatus, 0).list();
            List<Integer> collect = list.stream().filter(x -> parseObject(x.getModel()).getBooleanValue("schedule")).map(GameList::getId).collect(Collectors.toList());
            for (var gameId : collect) {
                var game = gameCache.getGameListCache(gameId);
                var model = parseObject(game.getModel()).getString("model");
                var platList = gameCache.getPlatListCache(game.getPlatListId());
                var plat = (PlatSportsAbstractFactory) PlatAbstractFactory.init(model, platList.getName());
                if (plat != null) {
                    var pullList = plat.getSportSchedule(reqPage, gameId);
                    sportSchedulesList.addAll(pullList);
                }
            }
        }
        sportSchedulesList = sportSchedulesList.stream().filter(sport ->
                Optional.ofNullable(reqDate.getStatus()).map(x -> sport.getStatus().equals(x)).orElse(true) &&
                        Optional.ofNullable(reqDate.getStartTime()).map(x -> sport.getTimestamp() >= x).orElse(true) &&
                        Optional.ofNullable(reqDate.getEndTime()).map(x -> sport.getTimestamp() <= x).orElse(true)
        ).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sportSchedulesList)) {
            return new ResPage<>();
        }
        //先按开赛时间排序
        sportSchedulesList.sort(Comparator.comparing(BetslipsDetailDto.SportSchedule::getTimestamp));
        var page = new Page<BetslipsDetailDto.SportSchedule>(dto.getCurrent(), dto.getSize(), sportSchedulesList.size());
        var sortField = ArrayUtils.isEmpty(dto.getSortField()) ? "sort" : dto.getSortField()[0];
        var sort2 = ArrayUtils.isEmpty(dto.getSortField()) || dto.getSortField().length <= 1 ? "operatorTime" : dto.getSortField()[1];
        var sort2Field = StringUtils.isEmpty(sort2) ? "timestamp" : "operatorTime";
        Stream<BetslipsDetailDto.SportSchedule> asc = dto.getSortKey() != null && dto.getSortKey().equals("ASC") ?
                sportSchedulesList.stream().sorted(Comparator.comparing(x -> sortField).thenComparing(x ->
                        sort2Field, Comparator.reverseOrder())) :
                sportSchedulesList.stream().sorted(Comparator.comparing(x ->
                        sortField).reversed().thenComparing(x -> sort2, Comparator.reverseOrder()));
        List<BetslipsDetailDto.SportSchedule> records = asc.skip(dto.getSize() * (dto.getCurrent() - 1))
                .limit(dto.getSize()).collect(Collectors.toList());
        page.setRecords(records);
        return ResPage.get(page);
    }

    /**
     * 修改热门赛事配置
     *
     * @param reqDto 请求参数
     * @return Boolean
     */
    @Override
    public Boolean updateSportCompetition(PlatManager.UpdateSportCompetitionRedDto reqDto) {
        var flag = false;
        var schedules = jedisUtil.hget(MODEL + SPORT_SCHEDULE, COMMON_TOTAL_HASH);
        var sportSchedulesList = parseArray(schedules, BetslipsDetailDto.SportSchedule.class);
        var now = DateNewUtils.now();
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        for (int i = 0; i < sportSchedulesList.size(); i++) {
            if (sportSchedulesList.get(i).getMEID().equals(reqDto.getMeid())) {
                var sportSchedule = BeanConvertUtils.copyProperties(sportSchedulesList.get(i), BetslipsDetailDto.SportSchedule::new,
                        (source, target) -> {
                            Optional.ofNullable(reqDto.getGameName()).ifPresent(x -> target.setGameName(reqDto.getGameName()));
                            Optional.ofNullable(reqDto.getStatus()).ifPresent(x -> target.setStatus(reqDto.getStatus()));
                            Optional.ofNullable(reqDto.getSort()).ifPresent(x -> target.setSort(reqDto.getSort()));
                            Optional.ofNullable(reqDto.getTimestamp()).ifPresent(x -> target.setTimestamp(reqDto.getTimestamp()));
                            var pant1 = new BetslipsDetailDto.SportSchedule.participant1();
                            Optional.ofNullable(reqDto.getHomeTeamName()).ifPresent(x -> pant1.setName(reqDto.getHomeTeamName()));
                            Optional.ofNullable(reqDto.getHomeTeamIcon()).ifPresent(x -> pant1.setIcon(reqDto.getHomeTeamIcon()));
                            target.setParticipant1(pant1);
                            var pant2 = new BetslipsDetailDto.SportSchedule.participant2();
                            Optional.ofNullable(reqDto.getVisitTeamName()).ifPresent(x -> pant2.setName(reqDto.getVisitTeamName()));
                            Optional.ofNullable(reqDto.getVisitTeamIcon()).ifPresent(x -> pant2.setIcon(reqDto.getVisitTeamIcon()));
                            target.setParticipant2(pant2);
                            target.setOperatorName(headerInfo.username);
                            target.setOperatorTime(now);
                        });
                sportSchedulesList.add(i, sportSchedule);
                flag = true;
                break;
            }
        }
        jedisUtil.hset(MODEL + SPORT_SCHEDULE, COMMON_TOTAL_HASH, toJSONString(sportSchedulesList));
        return flag;
    }

    /**
     * 删除热门赛事配置
     *
     * @param reqDto 请求参数
     * @return Boolean
     */
    @Override
    public Boolean deleteSportCompetition(PlatManager.DeleteSportCompetitionRedDto reqDto) {
        var flag = false;
        var schedules = jedisUtil.hget(MODEL + SPORT_SCHEDULE, COMMON_TOTAL_HASH);
        var sportSchedulesList = parseArray(schedules, BetslipsDetailDto.SportSchedule.class);
        for (int i = 0; i < sportSchedulesList.size(); i++) {
            if (sportSchedulesList.get(i).getMEID().equals(reqDto.getMeid())) {
                sportSchedulesList.remove(i);
                flag = true;
                break;
            }
        }
        jedisUtil.hset(MODEL + SPORT_SCHEDULE, COMMON_TOTAL_HASH, toJSONString(sportSchedulesList));
        return flag;
    }

    /**
     * 队徽列表
     *
     * @return List<PlatManager.TeamLogoDto>
     */
    @Override
    public ResPage<PlatManager.TeamLogoDto> teamLogonList(ReqPage<PlatManager.TeamLogoReqDto> reqDto) {
        var reqData = reqDto.getData();
        var page = teamLogoServiceImpl.page(reqDto.getPage(), new LambdaQueryWrapper<TeamLogo>()
                .likeRight(nonNull(reqData.getTeamLogoName()), TeamLogo::getTeamLogoName, reqData.getTeamLogoName())
                .orderByDesc(TeamLogo::getCreatedAt));
        var rePage = BeanConvertUtils.copyPageProperties(page, PlatManager.TeamLogoDto::new);
        return ResPage.get(rePage);
    }

    /**
     * 新增队徽
     *
     * @return Boolean
     */
    @Override
    public Boolean addTeamLogon(PlatManager.TeamLogoDto reqDto) {
        var teamLogo = BeanConvertUtils.copyProperties(reqDto, TeamLogo::new);
        var now = DateNewUtils.now();
        teamLogo.setCreatedAt(now);
        teamLogo.setUpdatedAt(now);
        return teamLogoServiceImpl.save(teamLogo);
    }

    /**
     * 批量删除队徽
     *
     * @return Boolean
     */
    @Override
    public Boolean deleteTeamLogon(PlatManager.DeleteTeamLogoDto reqDto) {
        var flag = false;
        var idLIst = Arrays.stream(reqDto.getIds().split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        flag = teamLogoServiceImpl.removeByIds(idLIst);
        if (flag) {
            Arrays.stream(reqDto.getTeamLogoUrls().split(",")).forEach(url ->
                    //删除上传服务器文件
                    fastDFSClientUtils.deleteFile(url)
            );
        }
        return flag;
    }
}
