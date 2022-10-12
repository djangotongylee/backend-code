package com.xinbo.sports.plat.service.impl.dg;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.dao.generator.po.BetslipsDg;
import com.xinbo.sports.dao.generator.service.BetslipsDgService;
import com.xinbo.sports.plat.service.impl.DGServiceImpl;
import com.xinbo.sports.utils.BeanConvertUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xinbo.sports.plat.io.bo.PlatFactoryParams.*;

/**
 * <p>
 * DG视讯
 * </p>
 *
 * @author andy
 * @since 2020/5/29
 */
@Service
public class DGLiveServiceImpl extends DGServiceImpl {
    @Resource
    private BetslipsDgService betslipsDgServiceImpl;


    private Page<PlatBetListResDto> processRecords(List<BetslipsDg> records) {
        Stream<PlatBetListResDto> streamList = records.stream().map(entity -> {
            PlatBetListResDto dto = BeanConvertUtils.beanCopy(entity, PlatBetListResDto::new);
            if (Objects.nonNull(entity) && Objects.nonNull(dto)) {
                dto.setName(id2Name(entity.getGameId(), entity.getTableId()));
            }
            return dto;
        });
        return (Page<PlatBetListResDto>) streamList.collect(Collectors.toList());
    }

//    private List<PlatBetListResDto> processRecords(List<BetslipsDg> records) {
//        List<PlatBetListResDto> list = new ArrayList<>();
//        for (BetslipsDg entity : records) {
//            PlatBetListResDto dto = BeanConvertUtils.beanCopy(entity, PlatBetListResDto::new);
//            if (Objects.nonNull(entity) && Objects.nonNull(dto)) {
//                dto.setName(id2Name(entity.getGameId(), entity.getTableId()));
//                list.add(dto);
//            }
//        }
//        return list;
//    }


    /**
     * 游戏Id -> 游戏名称
     *
     * @param gameId  游戏Id
     * @param tableId 游戏桌号
     */
    public String id2Name(Integer gameId, Integer tableId) {
        // 游戏名称
        String name = "";
        String code = gameId + "" + tableId;
        switch (code) {
            case "110101":
            case "110102":
            case "110103":
            case "110105":
            case "110106":
            case "110107":
            case "140103":

            case "150101":
            case "150102":
            case "150103":

            case "170101":
            case "170102":
            case "170103":
            case "170105":
            case "170106":
                name = "百家乐";
                break;
            case "130101":
            case "130102":
            case "130103":
            case "130105":
                name = "现场百家乐";
                break;
            case "310301":
            case "370301":
                name = "龙虎";
                break;
            case "410401":
            case "430401":
            case "450401":
            case "470401":
                name = "轮盘";
                break;
            case "510501":
            case "570501":
                name = "骰宝";
                break;
            case "710701":
                name = "斗牛";
                break;
            case "1111101":
                name = "炸金花";
                break;
            case "1211201":
                name = "极速骰宝";
                break;

            case "330301":
                name = "现场龙虎";
                break;
            case "140101":
            case "140102":
                name = "波贝百家乐";
                break;
            case "540501":
                name = "波贝骰宝";
                break;
            case "820801":
            case "820802":
            case "820803":
            case "820805":
                name = "竞咪百家乐";
                break;
            case "770701":
                name = "牛牛";
                break;
            case "1471401":
                name = "色碟";
                break;
            case "1571501":
                name = "鱼虾蟹";
                break;
            case "1":
                name = "会员发红包";
                break;
            case "2":
                name = "会员抢红包";
                break;
            case "3":
                name = "小费";
                break;
            case "4":
                name = "公司发红包";
                break;
            case "5":
                name = "博饼记录";
                break;
            default:
                break;
        }
        return name;
    }
}
