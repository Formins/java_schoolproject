package com.kevin.web;

import com.kevin.domain.Person;
import com.kevin.domain.PersonRepository;
import com.kevin.domain.User;
import com.kevin.domain.UserRepository;
import io.swagger.annotations.ApiOperation;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by kevin on 17/4/22.
 */
@RestController
//@RequestMapping(value="/users")
public class PersonController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;

    @ApiOperation(value = "查找所有通讯录用户")
    @GetMapping(value="/person")
    public List<Person> getPersonList(){
        return personRepository.findAll();
    }

    @ApiOperation(value = "查找所有专业")
    @GetMapping(value="/person/majors")
    public List<String> getMajorsList(){
        return personRepository.findAllMajors();
    }

    @ApiOperation(value = "查找专业所有person")
    @GetMapping(value="/person/majors/{major}")
    public List<Person> getPersonListByMajor(@PathVariable("major") String major){
        return personRepository.findAllByMajor(major);
    }


    @ApiOperation(value = "通过id查询用户信息",notes = "id为用户user的id")
    @GetMapping(value = "/personinfo/{id}")
    public Person getPerson(@PathVariable("id") Integer id) throws JSONException{
        User user = userRepository.findOne(id);
        Long username = user.getUsername();
        return personRepository.findByStudentNum(username);
    }

    @ApiOperation(value = "通过username查询用户信息")
    @GetMapping(value = "/personinfo/username/{username}")
    public Person getPersonByUsername(@PathVariable("username") Long username) throws JSONException{
        return personRepository.findByStudentNum(username);
    }



    @ApiOperation(value = "修改用户信息",notes = "id为用户user的id")
    @PutMapping(value = "/personinfo/{id}")
    public Map putPerson(@PathVariable("id") Integer id,
                          @RequestParam  Map map) throws JSONException {
        Map<String,Object> reMap = new HashMap<>();
        User user = userRepository.findOne(id);
        Long username = user.getUsername();
        Person person = personRepository.findByStudentNum(username);

        if(map.get("name")!=null){
            person.setName((String) map.get("name"));
        }
        if (map.get("age")!=null){
            person.setAge(Integer.parseInt((String) map.get("age")));
        }
        if(map.get("city")!=null){
            person.setCity((String) map.get("city"));
        }
        if(map.get("classes")!=null){
            person.setClasses((String) map.get("classes"));
        }
        if(map.get("email")!=null){
            person.setEmail((String) map.get("email"));
        }
        if(map.get("enterYear")!=null){
            person.setEnterYear(Integer.parseInt((String) map.get("enterYear")));
        }
        if(map.get("graduationYear")!=null){
            person.setGraduationYear(Integer.parseInt((String) map.get("graduationYear")));
        }
        if(map.get("major")!=null){
            person.setMajor((String) map.get("major"));
        }
        if(map.get("phoneNumber")!=null){
            person.setPhoneNumber(Long.parseLong((String)map.get("phoneNumber")));
        }
        if(map.get("sex")!=null){
            person.setSex((String) map.get("sex"));
        }
        if(map.get("workUnit")!=null){
            person.setWorkUnit((String) map.get("workUnit"));
        }
        personRepository.save(person);
        reMap.put("success",true);
        reMap.put("person", person);
        return reMap;
    }

    @ApiOperation(value = "删除用户")
    @DeleteMapping(value = "/person/{id}")
    public void deletePerson(@PathVariable("id") Integer id ){
        personRepository.delete(id);
    }

    @ApiOperation(value = "查找用户", notes = "根据用户的任意属性模糊匹配，返回List<Person>")
    @GetMapping(value = "/person/{attribute}")
    public List<Person> getPersonByAttribute(@PathVariable("attribute") String attribute){
        return personRepository.findByAttribute(attribute);
    }
}
