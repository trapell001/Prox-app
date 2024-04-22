1. tạo ra filter security

Ví dụ: 
   
static {
       GCSecurityConfig.addUriSecurity("/video/*", "POST", "DELETE");
       GCSecurityConfig.addUriSecurity("/video*", "POST", "DELETE");
       GCSecurityConfig.addUriSecurity("/music/*", "POST", "DELETE");
       GCSecurityConfig.addUriSecurity("/music*", "POST", "DELETE");
    }

2. Thêm config
@Autowired
private ConfigService configService;
public void init(){
    configService.addConfigActions("URL_FIRST_FILE",
        "https://data.nowtechai.com/assets/",
        "the first url will connect filename to become the complete url",
        s -> URL_FIRST_FILE = s
    );
}

    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    implementation 'com.google.firebase:firebase-admin:9.2.0'
    implementation group: 'io.jsonwebtoken', name: 'jjwt-api', version: '0.11.5'
    runtimeOnly group: 'io.jsonwebtoken', name: 'jjwt-impl', version: '0.11.5'
    runtimeOnly group: 'io.jsonwebtoken', name: 'jjwt-jackson', version: '0.11.5'
    implementation group: 'org.apache.httpcomponents.client5', name: 'httpclient5', version: '5.2.1'
    runtimeOnly 'com.h2database:h2'


check list to send discord
@AutoWrite
MonitorService monitorService;
monitorService.addListMapToCheck()

set time GMT+7
TimeService.setTimeGM7()

---
# update database
+ fix column type
> /setting/edit/h2/change-column-type?table=?&column=?&type=?

> table : name of table
> 
> colum :  column name of table
> 
> type : enum [INT, BIGINT, BOOLEAN, DOUBLE, VARCHAR, CHAR, LONGVARCHAR, BINARY, VARBINARY, BLOB, CLOB, DATE, TIME, TIMESTAMP]

+ get list table

> /setting/edit/h2/table

+ get column of table

> /setting/edit/h2/column?table=?
> 
> table : table name

---
# update file

> #ftp
>ftp.host=st37951.ispot.cc
> 
>ftp.username=ninhph@st37951.ispot.cc
> 
>ftp.password=Prox@689
> 
>ftp.path=/server

