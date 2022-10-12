package com.xinbo.sports.plat.service.impl.bti;

import com.alibaba.fastjson.JSONArray;
import com.xinbo.sports.dao.generator.po.GameList;
import com.xinbo.sports.dao.generator.po.TeamLogo;
import com.xinbo.sports.dao.generator.service.GameListService;
import com.xinbo.sports.dao.generator.service.TeamLogoService;
import com.xinbo.sports.plat.io.bo.BTIResponse;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.dto.base.BetslipsDetailDto;
import com.xinbo.sports.plat.service.impl.BTIServiceImpl;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.common.RedisConstant;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseArray;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.plat.io.enums.BTIPlatEnum.BTIMethodEnum.IMPLEMENTATION;
import static com.xinbo.sports.service.cache.KeyConstant.COMMON_TOTAL_HASH;
import static com.xinbo.sports.service.cache.KeyConstant.SPORT_SCHEDULE;
import static java.util.Objects.nonNull;

/**
 * @author: wells
 * @date: 2020/7/14
 * @description:
 */
@Slf4j
@Service
public class BTISportsServiceImpl extends BTIServiceImpl {
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private GameListService gameListServiceImpl;
    RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private TeamLogoService teamLogoServiceImpl;

    @Override
    public List<BetslipsDetailDto.SportSchedule> getSportSchedule(ReqPage<BetslipsDetailDto.SportScheduleReqDto> dto, Integer gameId) {
        List<BetslipsDetailDto.SportSchedule> sportSchedules = new ArrayList<>();

        try {
            var schedules = jedisUtil.hget(MODEL + SPORT_SCHEDULE, COMMON_TOTAL_HASH);
            int now = DateNewUtils.getGMT0();
            int i = DateUtils.strTranInt(DateUtils.yyyyMMdd(now) + " 23:59");
            var path = new StringBuilder();
            if (null == schedules) {
                String branchesList = config.getBranchesList();
                if (branchesList == null) {
                    return sportSchedules;
                } else {
                    JSONArray objects = parseArray(branchesList);
                    if (objects != null) {
                        for (int o = 0; o < objects.size(); o++) {
                            path.append("&BranchID=" + o);
                        }
                    }
                }
                var url = config.getXmlProxy() + path;
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.set(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE);
                // Accept-Encoding 头，表示客户端接收gzip格式的压缩
                httpHeaders.set(HttpHeaders.ACCEPT_ENCODING, "gzip");
                log.info(MODEL + "请求url:" + url);
                ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(httpHeaders), byte[].class);
                var results = GZIPUtils.uncompressToString(responseEntity.getBody());
                log.info(MODEL + "请求response:" + responseEntity);
                BTIResponse.Events response = XmlBuilder.xmlStrToObject(results, BTIResponse.Events::new);
                AtomicInteger sort = new AtomicInteger();
                GameList one = gameListServiceImpl.lambdaQuery().eq(GameList::getId, gameId).one();
                for (var x : response.getEvent()) {
                    x.setDateTimeGMT(String.valueOf(DateNewUtils.oriTimeZoneToDesTimeZone(x.getDateTimeGMT(), DateNewUtils.Format.dd_MM_yyyy_HH_mm_ss_SSS, "GMT", "+0", "UTC", "+8")));
                }
                //匹配队徽->名称对应url
                var teamMap = teamLogoServiceImpl.list().stream()
                        .collect(Collectors.toMap(TeamLogo::getTeamLogoName, TeamLogo::getTeamLogoUrl, (v1, v2) -> v1));
                List<BTIResponse.Event> collect = response.getEvent().stream().filter(x -> nonNull(x.getMoneyLine())).sorted(Comparator.comparing(BTIResponse.Event::getDateTimeGMT).reversed()).collect(Collectors.toList());
                sportSchedules = BeanConvertUtils.copyListProperties(collect, BetslipsDetailDto.SportSchedule::new, (sb, bo) -> {
                    BetslipsDetailDto.SportSchedule.participant1 participant1 = new BetslipsDetailDto.SportSchedule.participant1();
                    BetslipsDetailDto.SportSchedule.participant2 participant2 = new BetslipsDetailDto.SportSchedule.participant2();
                    var partName1 = sb.getParticipants().getParticipant1().getName();
                    var partName2 = sb.getParticipants().getParticipant2().getName();
                    participant1.setName(partName1);
                    participant2.setName(partName2);
                    participant1.setIcon(teamMap.getOrDefault(partName1, ""));
                    participant2.setIcon(teamMap.getOrDefault(partName2, ""));
                    bo.setParticipant1(participant1);
                    bo.setParticipant2(participant2);
                    bo.setMoneyLine(toJSONString(sb.getMoneyLine()));
                    bo.setSort(sort.getAndIncrement());
                    bo.setGameId(gameId);
                    bo.setStatus(0);
                    bo.setTimestamp(Integer.parseInt(sb.getDateTimeGMT()));
                    bo.setGameName(one != null ? one.getName() : null);
                });
                jedisUtil.expireByTimestamp(MODEL + SPORT_SCHEDULE, (long) (i * 1000));
                jedisUtil.hset(MODEL + SPORT_SCHEDULE, COMMON_TOTAL_HASH, toJSONString(sportSchedules));
            } else {
                List<BetslipsDetailDto.SportSchedule> sportSchedulesList = parseArray(schedules, BetslipsDetailDto.SportSchedule.class);
                sportSchedulesList.stream().filter(x -> x.getTimestamp() > DateNewUtils.now()).forEach(x -> jedisUtil.hdel(MODEL + SPORT_SCHEDULE, x.getMEID().toString()));
                sportSchedules = sportSchedulesList.stream().filter(x -> x.getTimestamp() > DateNewUtils.now()).collect(Collectors.toList());
            }
        } catch (Exception e) {
            if (e.getMessage().contains("403")) {
                log.error(CodeInfo.PLAT_IP_NOT_ACCESS.getMsg());
            } else {
                log.error(e.toString());
                log.error(CodeInfo.PLAT_SYSTEM_ERROR.getMsg());
            }
        }
        return sportSchedules;
    }

    @Override
    public BetslipsDetailDto.ForwardEvent forwardEvent(String username, Integer masterEventID) {
        var forwardEvent = new BetslipsDetailDto.ForwardEvent();
        var token = jedisUtil.get(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH + ":" + RedisConstant.BTI + ":" + username) == null
                || jedisUtil.get(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH + ":" + RedisConstant.BTI + ":" + username).isEmpty()
                ? getAuthToken(PlatFactoryParams.PlatLoginReqDto.builder().username(username).build())
                : jedisUtil.get(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH + ":" + RedisConstant.BTI + ":" + username);
        forwardEvent.setLink(config.getLogUrl() + IMPLEMENTATION.getMethodName() + masterEventID + "&token=" + token);
        return forwardEvent;
    }
}
