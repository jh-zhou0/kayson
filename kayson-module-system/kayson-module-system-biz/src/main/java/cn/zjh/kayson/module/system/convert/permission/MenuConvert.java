package cn.zjh.kayson.module.system.convert.permission;

import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuRespVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.MenuDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface MenuConvert {
    
    MenuConvert INSTANCE = Mappers.getMapper(MenuConvert.class);
    
    MenuDO convert(MenuCreateReqVO bean);
    
    MenuDO convert(MenuUpdateReqVO bean);

    MenuRespVO convert(MenuDO bean);

    List<MenuRespVO> convertList(List<MenuDO> list);
    
}
