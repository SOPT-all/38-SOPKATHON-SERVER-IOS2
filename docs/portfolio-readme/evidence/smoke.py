"""Replay only against the isolated loopback instance and its fresh PINGO seed."""
import json, urllib.request, urllib.error
BASE='http://127.0.0.1:18038'
def go(path, body=None):
    req=urllib.request.Request(BASE+path, data=None if body is None else json.dumps(body,ensure_ascii=False).encode(),headers={'Content-Type':'application/json'} if body else {})
    try: r=urllib.request.urlopen(req,timeout=8)
    except urllib.error.HTTPError as e: r=e
    return r.status,json.load(r)
def expect(label,path,status,key=None,body=None):
    s,r=go(path,body)
    assert s==status,(label,s,r)
    result=key(r) if key else r
    print(json.dumps({'step':label,'http':s,'result':result},ensure_ascii=False))
    return result
expect('health','/actuator/health',200,lambda r: r['status'])
expected=['/api/v1/spots/stories','/api/v1/stories/{storyId}','/api/v1/stories/{storyId}/reactions']
openapi=expect('openapi','/v3/api-docs',200,lambda r:[p for p in expected if p in r['paths']])
assert openapi==expected
for sort in ['latest','popular']:
    expect('sort '+sort,f'/api/v1/spots/stories?userId=30&sort={sort}',200,lambda r:[x['storyId'] for x in r['data']['stories']])
detail='/api/v1/stories/1?userId=30'
view=lambda r:(r['data'].get('myReactionType'),r['data']['reactionCounts'])
before=expect('before',detail,200,view)
apply='/api/v1/stories/1/reactions'
assert before[0] is None
like=expect('add LIKE',apply,200,view,{'userId':30,'reactionType':'LIKE'})
assert like[0]=='LIKE' and like[1]['LIKE']==before[1]['LIKE']+1
sad=expect('switch SAD',apply,200,view,{'userId':30,'reactionType':'SAD'})
assert sad[0]=='SAD' and sad[1]['LIKE']==before[1]['LIKE'] and sad[1]['SAD']==before[1]['SAD']+1
cancel=expect('cancel SAD',apply,200,view,{'userId':30,'reactionType':'SAD'})
assert cancel==before
assert expect('reload',detail,200,view)==before
story={'userId':30,'title':'빗소리','content':'비를 피하던 처마에서 이름 모를 고양이가 잠들었다.','storyType':'MEMORY'}
create=expect('create','/api/v1/spots/stories',201,lambda r:r['data']['storyId'],story)
confirm=expect('reopen',f'/api/v1/stories/{create}?userId=30',200,lambda r:{k:r['data'][k] for k in ['storyId','title','content','storyType']})
assert confirm.pop('storyId')==create and confirm=={k:story[k] for k in confirm}
assert expect('latest after create','/api/v1/spots/stories?userId=30&sort=latest',200,lambda r:r['data']['stories'][0]['storyId'])==create
expect('invalid title','/api/v1/spots/stories',400,lambda r:r.get('code'),dict(story,title='띄어 쓰기'))
