package com.ksptooi.flr.proc.service.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ksptooi.flr.entity.player.PlayerDetail;
import com.ksptooi.flr.entity.player.PlayerLocation;
import com.ksptooi.flr.mapper.player.PlayerDetailMapper;
import com.ksptooi.flr.mapper.player.PlayerLocationMapper;
import com.ksptooi.flr.mapper.player.PlayerMapper;
import com.ksptooi.flr.entity.player.FLRPlayer;
import com.ksptooi.flr.proc.exception.AuthException;
import com.ksptooi.flr.util.DateUtil;
import com.ksptooi.flr.entity.status.ErrorStatus;
import com.ksptooi.flr.entity.status.AuthState;
import com.ksptooi.flr.util.DtoUtil;
import org.bukkit.entity.Player;
import org.mybatis.guice.transactional.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;

@Singleton
@Transactional
public class PlayerServiceBlock implements PlayerService {


    @Inject
    PlayerMapper mapper = null;

    @Inject
    PlayerLocationMapper locMapper = null;

    @Inject
    PlayerDetailMapper detailMapper = null;


    /**
     * 玩家注册
     * @param bukkitPlayer
     * @return 注册
     */
    @Override
    public FLRPlayer playerRegister(Player bukkitPlayer,String pwd) throws AuthException {

        FLRPlayer player = DtoUtil.toPlayer(null, bukkitPlayer);


        //判断是否已经注册
        if(mapper.getPlayerByAccount(player.getAccount()) != null){
            throw new AuthException(ErrorStatus.AUTH_ALREADY_REG);
        }

        int insertPlayer = 0;
        int insertPlayerLoc = 0;
        int insertDetail = 0;

        //注册业务流程
        player.setPassword(pwd);
        player.setLastLoginDate(DateUtil.getCurTimeString());
        player.setRegisterDate(DateUtil.getCurTimeString());
        player.setAuthStatus(AuthState.LOGIN_DONE.getCode());
        player.setLoginCount(AuthState.DEFAULT_LOGIN_COUNT.getCode());

        //添加用户进表
        insertPlayer = mapper.insertPlayer(player);

        //添加用户位置进表(使用主键回填值)
        PlayerLocation loc = new PlayerLocation();
        loc.setPid(player.getPid());

        insertPlayerLoc = locMapper.insertLocation(loc);

        //添加用户详细进表
        PlayerDetail playerDetail = new PlayerDetail();
        playerDetail.setPid(player.getPid());
        insertDetail = detailMapper.insertDetail(playerDetail);

        //判断添加是否成功 如果不成功则回滚事务
        if(insertPlayer<1 || insertPlayerLoc<1 || insertDetail<1){
            throw new AuthException(ErrorStatus.FATAL_DB);
        }

        //添加成功则返回玩家实例
        return player;
    }

    /**
     * 玩家登录
     * @param playerAccount
     * @param pwd
     * @return 成功返回玩家实例 失败返回null
     */
    @Override
    public FLRPlayer playerLogin(String playerAccount, String pwd) throws AuthException {

        FLRPlayer playerByName = mapper.getPlayerByAccount(playerAccount);

        //玩家不存在
        if(playerByName == null){
            throw new AuthException(ErrorStatus.AUTH_NO_REG);
        }

        //密码判断
        if(!playerByName.getPassword().equals(pwd)){
            throw new AuthException(ErrorStatus.AUTH_PWD_INVALID);
        }

        //已经登录
        if(playerByName.isLogin()){
            throw new AuthException(ErrorStatus.AUTH_ALREADY_LOG);
        }


        //修改数据库中玩家的状态
        playerByName.setLastLoginDate(DateUtil.getCurTimeString());
        playerByName.setLoginCount(playerByName.getLoginCount()+1);

        //修改数据库中的登录状态
        playerByName.setAuthStatus(AuthState.LOGIN_DONE.getCode());

        mapper.updatePlayer(playerByName);

        return playerByName;

    }

    /**
     * 玩家登出
     * @param playerName
     * @return 成功返回true 失败返回false
     */
    @Override
    public boolean playerLogout(String playerName) {

        FLRPlayer playerByName = mapper.getPlayerByAccount(playerName);

        playerByName.setLeaveDate(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        //playerByName.setLoginStatus(PlayerStatus.LOGIN_FAILED.getCode());
        playerByName.setAuthStatus(AuthState.LOGIN_DONE.getCode());

        return true;
    }

    /**
     * 根据玩家名称获取玩家数据对象
     * @param playerName
     * @return 玩家数据对象
     */
    @Override
    public FLRPlayer getFLRPlayer(String playerName) {
        return mapper.getPlayerByAccount(playerName);
    }

}
