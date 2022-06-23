from docs_conf.conf import *

#branch configuration

branch = 'latest'

linkcheck_ignore = [
    'http://localhost.*',
    'http://127.0.0.1.*',
    'https://gerrit.o-ran-sc.org.*'
]

extensions = ['sphinx.ext.intersphinx',]

#intershpinx mapping with other projects
intersphinx_mapping = {}

intersphinx_mapping['nonrtric'] = ('https://docs.o-ran-sc.org/projects/o-ran-sc-nonrtric/en/%s' % branch, None)
