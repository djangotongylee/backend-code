package com.xinbo.sports.backend.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.backend.io.dto.AgentDto;
import com.xinbo.sports.backend.io.dto.UserGroupParameter;
import com.xinbo.sports.backend.io.dto.UserGroupParameter.DeleteUserGroupReqDto;
import com.xinbo.sports.backend.io.dto.UserGroupParameter.SaveOrUpdateUserGroupReqDto;
import com.xinbo.sports.backend.io.dto.UserGroupParameter.UserGroupListReqDto;
import com.xinbo.sports.backend.io.dto.UserGroupParameter.UserGroupListResDto;
import com.xinbo.sports.backend.mapper.AdminPermissionMapper;
import com.xinbo.sports.backend.mapper.AuthManagerMapper;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.AdminCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;

/**
 * @Description :
 * @Date 2020/3/13 20:22
 * @Created by WELLS
 */
@Slf4j
@Component
public class AuthManagerServiceImpl {

    @Autowired
    private AuthManagerMapper authManagerMapper;
    @Resource
    private JedisUtil jedisUtil;
    @Resource
    private UserService userServiceImpl;
    @Resource
    private UserProfileService userProfileServiceImpl;
    @Resource
    private AuthAdminGroupService authAdminGroupServiceImpl;
    @Resource
    private AdminCache adminCache;
    @Resource
    private AuthGroupAccessService authGroupAccessServiceImpl;
    @Resource
    private AuthGroupService authGroupServiceImpl;
    @Resource
    private AdminService adminServiceImpl;
    @Resource
    private AdminPermissionMapper adminPermissionMapper;
    @Resource
    private AdminPermissionServiceImpl adminPermissionServiceImpl;

    /**
     * ??????????????????-true;?????????-false
     */
    private ThreadLocal<Boolean> isTransPerm = new ThreadLocal<Boolean>();
    /**
     * ?????????
     */
    public static final ThreadLocal<List<String>> PID_LIST = new ThreadLocal<>();

    /**
     * ??????????????????????????????
     *
     * @param map
     * @return
     */
    public JSONObject queryUserRule(Map<String, Object> map) {
        String uid = Optional.ofNullable(map.get("uid")).orElse("").toString();
        JSONObject jo = new JSONObject();
        if (StringUtils.isBlank(uid)) {
            log.error("uid is blank");
            jo.put("msg", "uid????????????");
            jo.put("msg_zh", "uid is null");
            return jo;
        }
        Map<String, Object> resultMap = authManagerMapper.queryUserRule(map);
        return DataTransformaUtils.transformaJO(resultMap);
    }

    /*??????????????????????????????*/
    public JSONObject updateUserRule(Map<String, Object> map) {
        String uid = Optional.ofNullable(map.get("uid")).orElse("").toString();
        JSONObject jo = new JSONObject();
        if (StringUtils.isBlank(uid)) {
            log.error("uid is blank");
            jo.put("msg", "uid????????????");
            jo.put("msg_zh", "uid is null");
            return jo;
        }
        try {
            authManagerMapper.updateUserRule(map);
            jo.put("msg_zh", GlobalVariableUtils.UPDATE_SUCCESS);
        } catch (Exception e) {
            jo.put("msg", e.getMessage());
            jo.put("msg_zh", GlobalVariableUtils.UPDATE_FAIL);
        }
        return jo;
    }


    /**
     * ??????uid???????????????????????????????????????
     *
     * @param map
     * @return
     */
    public JSONObject loadAuthRule(Map<String, Object> map) {
        String uid = map.getOrDefault("uid", "").toString();
        if (StringUtils.isBlank(uid)) {
            JSONObject jo = new JSONObject();
            jo.put("msg", "uid is null");
            jo.put("msg_zh", "?????????uid");
            return jo;
        }
        var permissionJson = jedisUtil.hget(Constant.PERMISSION_LIST, uid);
        if (StringUtils.isNotEmpty(permissionJson)) {
            return parseObject(permissionJson);
        }
        /*???????????????????????????????????????*/
        Map<String, Object> repairMap = authManagerMapper.queryRepair(map);
        String rules = "";
        if (repairMap != null) {
            String menu_id = Optional.ofNullable(repairMap.get("menu_id")).orElse("").toString();
            String[] rulesArray = menu_id.split(",");
            /*   String[] rulesArray = queryRulesByUid(map);*/
            StringBuffer sb = new StringBuffer("");
            for (int i = 0, length = rulesArray.length; i < length; i++) {
                if (i == length - 1) {
                    sb.append(rulesArray[i]);
                } else {
                    sb.append(rulesArray[i]).append(",");
                }
            }
            rules = sb.toString();
        }
        if (StringUtils.isBlank(rules)) {
            throw new BusinessException(CodeInfo.NO_AUTH_TO_GET_RULES);
        }
        map.put("rules", rules);
        List<Map<String, Object>> list = authManagerMapper.queryAllRule(map);
        JSONObject root = new JSONObject();
        root.put("id", 0);
        JSONArray ja = DataTransformaUtils.transformaJA(list);
        isTransPerm.set(true);
        JSONObject children;
        try {
            PID_LIST.set(new ArrayList<>());
            children = getChildren(root, ja);
        } finally {
            isTransPerm.remove();
        }
        jedisUtil.hset(Constant.PERMISSION_LIST, uid, toJSONString(children));
        return children;
    }

    /**
     * ?????????????????????????????????
     *
     * @param map
     * @return
     */
    public JSONObject queryAllRuleTitle(Map<String, Object> map) {
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var admin = adminCache.getAdminInfoById(headerInfo.getId());
        var ruleList = new ArrayList<String>();
        var adminGroupId = Optional.ofNullable(map.get("adminGroupId")).map(x -> Integer.parseInt(x + "")).orElse(admin.getAdminGroupId());
        var groupId = Optional.ofNullable(map.get("groupId")).map(x -> Integer.parseInt(x + "")).orElse(0);
        var adminGroup = authAdminGroupServiceImpl.getById((adminGroupId));
        ruleList.addAll(Arrays.asList(Optional.ofNullable(adminGroup).map(AuthAdminGroup::getRules).orElse("")
                .split(",")));
        if (groupId != 0) {
            var group = authGroupServiceImpl.getById(groupId);
            ruleList.clear();
            ruleList.addAll(Arrays.asList(Optional.ofNullable(group).map(AuthGroup::getRules).orElse("")
                    .split(",")));
        }
        List<Map<String, Object>> list = authManagerMapper.queryAllRuleTitle(map);
        list = list.stream().filter(x ->
                (adminGroupId == 0 && groupId == 0) || ruleList.contains(x.get("id") + ""))
                .collect(Collectors.toList());
        JSONObject root = new JSONObject();
        root.put("id", 0);
        JSONArray ja = DataTransformaUtils.transformaJA(list);
        isTransPerm.set(false);
        JSONObject children;
        try {
            PID_LIST.set(new ArrayList<>());
            children = getChildren(root, ja);
        } finally {
            isTransPerm.remove();
        }
        return children;
    }

    /**
     * ??????uid??????????????????????????????id??????
     *
     * @param map
     * @return
     */
    public String[] queryHasAuthByUid(Map<String, Object> map) {
        String uid = Optional.ofNullable(map.get("uid")).orElse("").toString();
        if (StringUtils.isBlank(uid)) {
            return null;
        }
        Map<String, Object> repairMap = authManagerMapper.queryRepair(map);
        String menu_id = Optional.ofNullable(repairMap.get("menu_id")).orElse("").toString();
        String[] split = menu_id.split(",");
        List<Map<String, Object>> list = authManagerMapper.queryMenuId();
        Set<String> set = new HashSet<>();
        for (Map<String, Object> item : list) {
            set.add(String.valueOf(item.get("pid")));
        }
        ArrayList<String> roleIds = new ArrayList<>();
        for (String roleId : split) {
            /*?????????*/
            if (!set.contains(roleId)) {
                roleIds.add(roleId);
            }
        }
        return roleIds.toArray(new String[roleIds.size()]);
    }

    /**
     * ??????roleId??????????????????????????????id??????
     *
     * @param map
     * @return
     */
    public String[] queryHasAuthByRoleId(Map<String, Object> map) {
        String id = map.getOrDefault("id", "").toString();
        if (StringUtils.isBlank(id)) {
            JSONObject jo = new JSONObject();
            jo.put("msg", "id must be not null");
            jo.put("msg_zh", "?????????id");
            return null;
        }
        ArrayList<String> roleIds = new ArrayList<>();
        String rules = authManagerMapper.queryRulesByRoleId(map);
        if (rules != null) {
            String[] split = rules.split(",");
            List<Map<String, Object>> list = authManagerMapper.queryMenuId();
            Set<String> set = new HashSet<>();
            for (Map<String, Object> item : list) {
                set.add(String.valueOf(item.get("pid")));
            }
            for (String roleId : split) {
                /*?????????*/
                if (!set.contains(roleId)) {
                    roleIds.add(roleId);
                }
            }
        }

        return roleIds.toArray(new String[roleIds.size()]);
    }

    /**
     * ????????????
     *
     * @param map
     * @return
     */
    public JSONObject insertRole(Map<String, Object> map) {
        var title = map.get("title") + "";
        var count = authGroupServiceImpl.lambdaQuery()
                .eq(AuthGroup::getTitle, title)
                .count();
        if (count > 0) {
            throw new BusinessException(CodeInfo.ROLE_ALREADY_EXIST);
        }
        JSONObject jo = new JSONObject();
        try {
            String rules = Optional.ofNullable(map.get("rules")).orElse("").toString();
            if (StringUtils.isNotBlank(rules)) {
                String[] arr = rules.split(",");
                int[] ints = Arrays.asList(arr).stream().mapToInt(e -> Integer.parseInt(e)).sorted().toArray();
                StringBuffer sb = new StringBuffer("");
                for (int i = 0, length = ints.length; i < length; i++) {
                    if (i == length - 1) {
                        sb.append(ints[i]);
                    } else {
                        sb.append(ints[i]).append(",");
                    }
                }
                rules = sb.toString();
                map.put("rules", rules);
            }
            BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
            var authGroupAccess = authGroupAccessServiceImpl.getOne(new LambdaQueryWrapper<AuthGroupAccess>()
                            .eq(AuthGroupAccess::getUid, headerInfo.getId())
                    , false);
            map.put("pid", Optional.ofNullable(authGroupAccess).map(AuthGroupAccess::getGroupId).orElse(0));
            map.put("parent", headerInfo.getId());
            map.put("operate_id", headerInfo.getId());
            authManagerMapper.insertRole(map);
            jo.put("msg_zh", GlobalVariableUtils.INSERT_SUCCESS);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            jo.put("msg", e.getMessage());
            jo.put("msg_zh", GlobalVariableUtils.INSERT_FAIL);
        }
        return jo;
    }

    /**
     * ????????????
     *
     * @param map
     * @return
     */
    public JSONObject updateRole(Map<String, Object> map) {
        String id = map.getOrDefault("id", "").toString();
        if (StringUtils.isBlank(id)) {
            JSONObject jo = new JSONObject();
            jo.put("msg", "id must be not null");
            jo.put("msg_zh", "?????????id");
            return jo;
        }
        JSONObject jo = new JSONObject();
        try {
            String rules = Optional.ofNullable(map.get("rules")).orElse("").toString();
            if (StringUtils.isNotBlank(rules)) {
                String[] arr = rules.split(",");
                int[] ints = Arrays.asList(arr).stream().mapToInt(e -> Integer.parseInt(e)).sorted().toArray();
                StringBuffer sb = new StringBuffer("");
                for (int i = 0, length = ints.length; i < length; i++) {
                    if (i == length - 1) {
                        sb.append(ints[i]);
                    } else {
                        sb.append(ints[i]).append(",");
                    }
                }
                rules = sb.toString();
                map.put("rules", rules);
            }
            BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
            map.put("operateId", headerInfo.id);
            authManagerMapper.updateRole(map);
            jo.put("msg_zh", GlobalVariableUtils.UPDATE_SUCCESS);
        } catch (Exception e) {
            log.error("", e);
            jo.put("msg", e.getMessage());
            jo.put("msg_zh", GlobalVariableUtils.UPDATE_FAIL);
        }
        jedisUtil.del(Constant.PERMISSION_LIST);
        return jo;
    }

    /**
     * ????????????
     *
     * @param map
     * @return
     */
    public JSONObject deleteRole(Map<String, Object> map) {
        String id = map.getOrDefault("id", "").toString();
        if (StringUtils.isBlank(id)) {
            JSONObject jo = new JSONObject();
            jo.put("msg", "id must be not null");
            jo.put("msg_zh", "?????????id");
            return jo;
        }
        var count = authGroupAccessServiceImpl.lambdaQuery()
                .eq(AuthGroupAccess::getGroupId, Integer.parseInt(id)).count();
        if (count > 0) {
            throw new BusinessException(CodeInfo.ROLE_DELETE_VERIFICATION);
        }
        JSONObject jo = new JSONObject();
        try {
            authManagerMapper.deleteRole(map);
            authGroupAccessServiceImpl.remove(new LambdaQueryWrapper<AuthGroupAccess>()
                    .eq(AuthGroupAccess::getGroupId, id));
            jo.put("msg_zh", GlobalVariableUtils.DELETE_SUCCESS);
        } catch (Exception e) {
            jo.put("msg", e.getMessage());
            jo.put("msg_zh", GlobalVariableUtils.DELETE_FAIL);
        }
        jedisUtil.del(Constant.PERMISSION_LIST);
        return jo;
    }


    /**
     * ?????????????????????
     *
     * @param root
     * @param ja
     * @return
     */

    public JSONObject getChildren(JSONObject root, JSONArray ja) {
        JSONArray children = new JSONArray();
        JSONArray button = new JSONArray();
        boolean isButton = false;
        for (int i = 0; i < ja.size(); i++) {
            JSONObject jo = (JSONObject) ja.get(i);
            if (String.valueOf(jo.get("pid")).equals(String.valueOf(root.get("id")))) {
                /*????????????????????????????????????button??????????????????,isButton???true?????????????????????button*/
                if (isTransPerm.get() && StringUtils.equals("1", String.valueOf(jo.get("isRaceMenu")))) {
                    isButton = true;
                }
                /*?????????????????????,button?????????????????????,????????????button*/
                if (isButton) {
                    button.add(jo);
                    isButton = false;
                } else {
                    PID_LIST.get().add(String.valueOf(root.get("id")));
                    /*???button??????????????????????????????????????????*/
                    children.add(getChildren(jo, ja));
                }
            }
        }
        if (!CollectionUtils.isEmpty(children)) {
            root.put("children", children);
        }
        if (!CollectionUtils.isEmpty(button)) {
            root.put("button", button);
        }
        return root;
    }

    /**
     * ?????????????????????????????????id??????
     *
     * @param map
     * @return
     */
    private String[] queryRulesByUid(Map<String, Object> map) {
        /*?????????????????????id??????,??????????????????id??????*/
        Map<String, Object> rulesMap = authManagerMapper.queryRulesByUid(map);
        String roleRules = Optional.ofNullable(rulesMap.get("roleRules")).orElse("").toString();
        String userRules = Optional.ofNullable(rulesMap.get("userRules")).orElse("").toString();
        String[] roleRulesArray = roleRules.split(",");
        /*????????????,roleRulesArray???set*/
        Set<String> itemSet = new HashSet<>(Arrays.asList(roleRulesArray));

        /*????????????????????????id,???????????????????????????*/
        if (StringUtils.isNotBlank(userRules)) {
            String[] userRulesArray = userRules.split(",");
            for (String rule : userRulesArray) {
                if (rule.contains("-")) {
                    String absoluteRule = rule.replace("-", "");
                    if (itemSet.contains(absoluteRule)) {
                        itemSet.remove(absoluteRule);
                    }
                } else {
                    String absoluteRule = rule.replace("+", "");
                    if (!itemSet.contains(absoluteRule)) {
                        itemSet.add(rule);
                    }
                }
            }
        }
        return itemSet.stream().toArray(String[]::new);
    }

    /**
     * ??????????????????
     *
     * @return ??????
     */
    public List<AgentDto.AgentListResDto> agentList() {
        var uidList = userProfileServiceImpl.lambdaQuery()
                .eq(UserProfile::getStatus, 10)
                .list()
                .stream()
                .map(UserProfile::getUid)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(uidList)) {
            return new ArrayList<>();
        }
        var userList = userServiceImpl.lambdaQuery()
                .in(User::getRole, List.of(1, 4))
                .in(User::getId, uidList)
                .list();
        return BeanConvertUtils.copyListProperties(userList, AgentDto.AgentListResDto::new, (user, resDto) -> {
            resDto.setAgentId(user.getId());
            resDto.setAgentName(user.getUsername());
        });
    }

    /**
     * ?????????????????????
     */
    public ResPage<UserGroupListResDto> userGroupList(ReqPage<UserGroupListReqDto> reqDto) {
        var sortArr = reqDto.getSortField();
        var orderFlag = true;
        if (ArrayUtils.isNotEmpty(sortArr)) {
            orderFlag = !sortArr[0].equals("updatedAt");
        }
        var reqData = reqDto.getData();
        var query = new LambdaQueryWrapper<AuthAdminGroup>()
                .eq(Objects.nonNull(reqData.getTitle()), AuthAdminGroup::getTitle, reqData.getTitle())
                .orderByDesc(orderFlag, AuthAdminGroup::getUpdatedAt);
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var admin = adminCache.getAdminInfoById(headerInfo.getId());
        var adminGroupList = authAdminGroupServiceImpl.lambdaQuery().eq(AuthAdminGroup::getStatus, 1).list();
        if (admin.getAdminGroupId() != 0) {
            var set = new HashSet<Integer>();
            set.add(admin.getAdminGroupId());
            var pidMap = adminGroupList.stream().collect(Collectors.groupingBy(AuthAdminGroup::getPid));
            getAuthAdminGroupSelfChild(pidMap, admin.getAdminGroupId(), set);
            //???????????????????????????????????????????????????????????????????????????????????????
            adminGroupList = adminGroupList.stream().filter(x -> adminPermissionServiceImpl.isVisible(admin, x.getId()) ||
                    set.contains(x.getId())).collect(Collectors.toList());
        }
        var idList = adminGroupList.stream().map(AuthAdminGroup::getId).collect(Collectors.toList());
        query.in(!CollectionUtils.isEmpty(idList), AuthAdminGroup::getId, idList);
        Page<AuthAdminGroup> page = authAdminGroupServiceImpl.page(reqDto.getPage(), query);
        //??????????????????PID
        queryAllRuleTitle(new HashMap<>());
        Page<UserGroupListResDto> adminGroupPage = BeanConvertUtils.copyPageProperties(page, UserGroupListResDto::new, (source, target) -> {
            var operateAdmin = adminCache.getAdminInfoById(source.getOperateId());
            var operateName = Constant.SUPER_ADMIN.equals(Optional.ofNullable(operateAdmin).map(x -> x.getAdminGroupId() + "").orElse("")) ?
                    "--" : Optional.ofNullable(operateAdmin).map(Admin::getUsername).orElse("");
            target.setOperateName(operateName);
            var rules = source.getRules();
            if (StringUtils.isNotEmpty(rules)) {
                var ruleList = Arrays.stream(rules.split(",")).filter(ele -> !PID_LIST.get().contains(ele))
                        .collect(Collectors.toList());
                target.setRules(StringUtils.join(ruleList, ","));
            }
        });
        return ResPage.get(adminGroupPage);
    }

    /**
     * ????????????????????????
     */
    public boolean saveOrUpdateUserGroup(SaveOrUpdateUserGroupReqDto reqDto) {
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var admin = adminCache.getAdminInfoById(headerInfo.getId());
        //ID????????????????????????
        var now = DateUtils.getCurrentTime();
        var adminGroup = BeanConvertUtils.copyProperties(reqDto, AuthAdminGroup::new);
        adminGroup.setUpdatedAt(now);
        adminGroup.setOperateId(headerInfo.getId());
        Optional.ofNullable(reqDto.getTitle()).ifPresent(title -> {
            var count = authAdminGroupServiceImpl.lambdaQuery().
                    eq(AuthAdminGroup::getTitle, title).count();
            if (count > 0) {
                throw new BusinessException(CodeInfo.ADMIN_GROUP_EXIST);
            }
        });
        if (Objects.isNull(reqDto.getId())) {
            adminGroup.setPid(admin.getAdminGroupId());
            adminGroup.setParent(headerInfo.getId());
            adminGroup.setCreatedAt(now);
            return authAdminGroupServiceImpl.save(adminGroup);
        }

        //?????????????????????????????????????????????
        if (StringUtils.isNotEmpty(reqDto.getRules())) {
            var groupList = authGroupServiceImpl.lambdaQuery()
                    .eq(AuthGroup::getAdminGroupId, reqDto.getId())
                    .list();
            groupList.forEach(group -> {
                if (StringUtils.isNotEmpty(group.getRules())) {
                    var updateRules = Arrays.asList(reqDto.getRules().split(","));
                    var groupRules = Arrays.stream(group.getRules().split(","))
                            .filter(updateRules::contains)
                            .collect(Collectors.toList());
                    group.setRules(StringUtils.join(groupRules, ","));
                    jedisUtil.del(Constant.PERMISSION_LIST);
                }
            });
            authGroupServiceImpl.updateBatchById(groupList);
            jedisUtil.hdel(KeyConstant.ADMIN_GROUP_ID_HASH, reqDto.getId() + "");
        } else {
            //?????????????????????
            if (reqDto.getId() == 2) {
                throw new BusinessException(CodeInfo.AGENT_GROUP_NO_UPDATE);
            }
        }
        return authAdminGroupServiceImpl.updateById(adminGroup);
    }

    /**
     * ???????????????
     */
    public boolean deleteUserGroup(DeleteUserGroupReqDto reqDto) {
        var count = authGroupServiceImpl.lambdaQuery()
                .eq(AuthGroup::getStatus, 1)
                .eq(AuthGroup::getAdminGroupId, reqDto.getId()).count();
        if (count > 0) {
            throw new BusinessException(CodeInfo.ADMIN_GROUP_DELETE_VERIFICATION);
        }
        jedisUtil.hdel(KeyConstant.ADMIN_GROUP_ID_HASH, reqDto.getId() + "");
        return authAdminGroupServiceImpl.removeById(reqDto.getId());
    }

    /**
     * ????????????????????????
     */
    public List<UserGroupParameter.AllAdminGroupListResDto> allAdminGroupList() {
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var admin = adminCache.getAdminInfoById(headerInfo.getId());
        var adminGroupList = authAdminGroupServiceImpl.lambdaQuery().eq(AuthAdminGroup::getStatus, 1).list();
        var set = new HashSet<Integer>();
        set.add(admin.getAdminGroupId());
        var pidMap = adminGroupList.stream().collect(Collectors.groupingBy(AuthAdminGroup::getPid));
        getAuthAdminGroupSelfChild(pidMap, admin.getAdminGroupId(), set);
        //???????????????????????????????????????????????????????????????????????????????????????
        adminGroupList = adminGroupList.stream().filter(x ->
                adminPermissionServiceImpl.isVisible(admin, x.getId()) || set.contains(x.getId()))
                .collect(Collectors.toList());
        return BeanConvertUtils.copyListProperties(adminGroupList, UserGroupParameter.AllAdminGroupListResDto::new);
    }

    /**
     * ????????????uid
     *
     * @param pidMap ??????Map
     * @param pid    ??????ID
     * @param ids    ????????????
     * @return
     */
    public HashSet<Integer> getAuthAdminGroupSelfChild(Map<Integer, List<AuthAdminGroup>> pidMap, Integer pid, HashSet<Integer> ids) {
        var list = pidMap.get(pid);
        if (!CollectionUtils.isEmpty(list)) {
            ids.add(pid);
            ids.addAll(list.stream().map(AuthAdminGroup::getId).collect(Collectors.toList()));
            list.forEach(admin -> getAuthAdminGroupSelfChild(pidMap, admin.getId(), ids));
        }
        return ids;
    }

    /**
     * ?????????????????????
     */
    public List<UserGroupParameter.AllRoleListResDto> allRoleList() {
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var admin = adminCache.getAdminInfoById(headerInfo.getId());
        var list = authGroupServiceImpl.lambdaQuery().eq(AuthGroup::getStatus, 1).list();
        var authGroupAccess = authGroupAccessServiceImpl.getOne(new LambdaQueryWrapper<AuthGroupAccess>().eq(AuthGroupAccess::getUid, admin.getId())
                , false);
        var set = new HashSet<Integer>();
        if (Objects.nonNull(authGroupAccess)) {
            set.add(authGroupAccess.getGroupId());
            var pidMap = list.stream().collect(Collectors.groupingBy(AuthGroup::getPid));
            adminPermissionServiceImpl.getGroupSelfChild(pidMap, authGroupAccess.getGroupId(), set);
        }
        //???????????????????????????????????????????????????????????????????????????????????????
        list = list.stream().filter(x -> adminPermissionServiceImpl.isVisible(admin, x.getAdminGroupId()) || set.contains(x.getId())).collect(Collectors.toList());
        return BeanConvertUtils.copyListProperties(list, UserGroupParameter.AllRoleListResDto::new);
    }


    /**
     * ??????googleSecret
     *
     * @return UserGroupParameter.GenerateGoogleSecretDto
     */
    public UserGroupParameter.GenerateGoogleSecretDto generateGoogleSecret() {
        return UserGroupParameter.GenerateGoogleSecretDto.builder()
                .secret(GoogleAuthenticator.getRandomSecretKey())
                .build();
    }
}
